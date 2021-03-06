/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.eeitt

import akka.stream.Materializer
import com.kenshoo.play.metrics.{ Metrics, MetricsFilter, MetricsFilterImpl, MetricsImpl, MetricsController }
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import org.apache.commons.codec.binary.Base64
import play.api.{ Application, BuiltInComponentsFromContext, BuiltInComponents, DefaultApplication, Configuration, Environment, LoggerConfigurator, Logger, Mode, Play, GlobalSettings }
import play.api.inject.{ Injector, SimpleInjector }
import play.api.http.{ DefaultHttpErrorHandler, DefaultHttpRequestHandler, HttpConfiguration, HttpErrorHandler, HttpRequestHandler }
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.{ EssentialFilter, Request, Handler, RequestHeader, Result }
import play.api.i18n.{ I18nComponents, I18nSupport, MessagesApi }
import play.api.ApplicationLoader.Context
import play.api.routing.Router
import play.core.SourceMapper
import play.filters.headers.SecurityHeadersFilter
import play.filters.csrf.{ CSRFComponents, CSRFFilter }
import play.twirl.api.Html
import scala.concurrent.Future
import uk.gov.hmrc.crypto.{ ApplicationCrypto, Crypted, PlainText }
import uk.gov.hmrc.play.audit.filters.FrontendAuditFilter
import uk.gov.hmrc.play.config.{ RunMode, ServicesConfig }
import uk.gov.hmrc.play.filters.{ CacheControlFilter, MicroserviceFilterSupport, RecoveryFilter }
import uk.gov.hmrc.play.filters.frontend.{ CSRFExceptionsFilter, DeviceIdFilter, HeadersFilter, CookieCryptoFilter }
import uk.gov.hmrc.play.frontend.bootstrap.ShowErrorPage
import uk.gov.hmrc.play.frontend.filters.{ DeviceIdCookieFilter, SecurityHeadersFilterFactory, SessionCookieCryptoFilter }
import uk.gov.hmrc.play.graphite.{ GraphiteConfig, GraphiteMetricsImpl }
import uk.gov.hmrc.play.audit.http.config.ErrorAuditingSettings
import uk.gov.hmrc.play.config.{ AppName, ControllerConfig }
import uk.gov.hmrc.play.health.AdminController
import uk.gov.hmrc.play.http.logging.filters.FrontendLoggingFilter
import uk.gov.hmrc.eeitt.controllers.{ EnrollmentVerificationController, EtmpDataLoaderProxy }
import uk.gov.hmrc.eeitt.testonly.SignOutController
import uk.gov.hmrc.eeitt.connectors.EeittConnector
import uk.gov.hmrc.eeitt.controllers.auth.SecuredActionsImpl
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import play.api.mvc.Results.NotImplemented

class ApplicationLoader extends play.api.ApplicationLoader {
  def load(context: Context) = {
    LoggerConfigurator(context.environment.classLoader).foreach { _.configure(context.environment) }
    (new BuiltInComponentsFromContext(context) with ApplicationModule).application
  }
}

class CustomHttpRequestHandler(
    router: Router,
    httpErrorHandler: HttpErrorHandler,
    httpConfiguration: HttpConfiguration,
    httpFilters: Seq[EssentialFilter]
) extends DefaultHttpRequestHandler(router, httpErrorHandler, httpConfiguration, httpFilters: _*) {
  override def routeRequest(request: RequestHeader): Option[Handler] = {
    router.handlerFor(request).orElse {
      Some(request.path).filter(_.endsWith("/")).flatMap(p => router.handlerFor(request.copy(path = p.dropRight(1))))
    }
  }
}

class CustomErrorHandling(
    val auditConnector: AuditConnector,
    val appName: String,
    environment: Environment,
    configuration: Configuration,
    sourceMapper: Option[SourceMapper] = None,
    router: => Option[Router] = None,
    appConfig: AppConfig,
    messages: MessagesApi
) extends DefaultHttpErrorHandler(environment, configuration, sourceMapper, router) with ErrorAuditingSettings with DoNothingHandler {
  import scala.concurrent.ExecutionContext.Implicits.global
  val showErrorPage = new ShowErrorPage with I18nSupport {
    val messagesApi: MessagesApi = messages
    override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: Request[_]): Html = {
      uk.gov.hmrc.eeitt.views.html.error_template(appConfig, pageTitle, heading, message)
    }
  }

  override def onBadRequest(request: RequestHeader, error: String): Future[Result] = {
    val errorPage = showErrorPage.onBadRequest(request, error)
    errorPage andThen {
      case x => super.onBadRequest(request, error) // We want run this just because of side effect in ErrorAuditingSettings
    }
  }

  override def onNotFound(request: RequestHeader, message: String): Future[Result] = {
    val errorPage = showErrorPage.onHandlerNotFound(request)
    errorPage andThen {
      case x => super.onHandlerNotFound(request) // We want run this just because of side effect in ErrorAuditingSettings
    }
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    val errorPage = showErrorPage.onError(request, exception)
    errorPage andThen {
      case x => super.onError(request, exception) // We want run this just because of side effect in ErrorAuditingSettings
    }
  }
}

/**
 * Sole purpose of this trait is to prevent calls to methods on GlobalSettings
 * by mixing it into ErrorAuditingSettings
 */
trait DoNothingHandler extends GlobalSettings {
  val response = Future.successful(NotImplemented)
  override def onError(request: RequestHeader, ex: Throwable): Future[Result] = response
  override def onHandlerNotFound(request: RequestHeader): Future[Result] = response
  override def onBadRequest(request: RequestHeader, error: String): Future[Result] = response
}

class Graphite(configuration: Configuration) extends GraphiteConfig {
  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = configuration.getConfig(s"microservice.metrics")
}

class FFilters(
    configuration: Configuration,
    metrics: Metrics,
    csrfFilter: CSRFFilter,
    val auditConnector: AuditConnector,
    appName: String
)(implicit materializer: Materializer) { self =>

  val metricsFilter: MetricsFilter = new MetricsFilterImpl(metrics)

  val deviceIdFilter: DeviceIdFilter = DeviceIdCookieFilter(appName, auditConnector)

  def frontendAuditFilter: FrontendAuditFilter = new AuditFilter(configuration, auditConnector)
  def loggingFilter: FrontendLoggingFilter = LoggingFilter

  def securityFilter: SecurityHeadersFilter = new SecurityHeadersFilterFactory {
    override def configuration = self.configuration
  }.newInstance

  lazy val enableSecurityHeaderFilter = configuration.getBoolean("security.headers.filter.enabled").getOrElse(true)

  def filters = if (enableSecurityHeaderFilter) Seq(securityFilter) ++ frontendFilters else frontendFilters

  def frontendFilters: Seq[EssentialFilter] = Seq(
    metricsFilter,
    HeadersFilter,
    SessionCookieCryptoFilter,
    deviceIdFilter,
    loggingFilter,
    frontendAuditFilter,
    CSRFExceptionsFilter,
    csrfFilter,
    CacheControlFilter.fromConfig("caching.allowedContentTypes"),
    RecoveryFilter
  )

  object ControllerConfiguration extends ControllerConfig {
    lazy val controllerConfigs = configuration.underlying.as[Config]("controllers")
  }

  class AuditFilter(
      override val appNameConfiguration: Configuration,
      override val auditConnector: AuditConnector
  ) extends FrontendAuditFilter with AppName {
    override def mat = materializer
    override lazy val maskedFormFields = Seq("password")
    override lazy val applicationPort = None

    override def controllerNeedsAuditing(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsAuditing
  }

  object LoggingFilter extends FrontendLoggingFilter {
    override def mat = materializer
    override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsLogging
  }
}

trait ApplicationModule extends BuiltInComponents
    with AhcWSComponents
    with I18nComponents
    with AppName
    with CSRFComponents
    with ServicesConfig { self =>

  override lazy val appNameConfiguration = configuration
  override lazy val mode: Mode.Mode = environment.mode
  override lazy val runModeConfiguration: Configuration = configuration

  Logger.info(s"Starting microservice : $appName : in mode : ${environment.mode}")

  if (environment.mode != Mode.Test) {
    new Graphite(configuration).onStart(configurationApp)
  }

  val auditConnector = new FrontendAuditConnector(materializer)

  val appConfig = new FrontendAppConfig(configuration)

  override lazy val httpErrorHandler: HttpErrorHandler = new CustomErrorHandling(auditConnector, appName, environment, configuration, sourceMapper, Some(router), appConfig, messagesApi)

  override lazy val httpRequestHandler: HttpRequestHandler = new CustomHttpRequestHandler(router, httpErrorHandler, httpConfiguration, httpFilters)

  override lazy val application: Application = new DefaultApplication(environment, applicationLifecycle, customInjector,
    configuration, httpRequestHandler, httpErrorHandler, actorSystem, materializer)

  // To avoid circular dependency when creating Graphite we will provide them this artificial
  // application. It is ok to do so since both of them are using mainly provided configuration.
  lazy val configurationApp = new Application() {
    def actorSystem = self.actorSystem
    def classloader = self.environment.classLoader
    def configuration = self.configuration
    def errorHandler = self.httpErrorHandler
    implicit def materializer = self.materializer
    def mode = self.environment.mode
    def path = self.environment.rootPath
    def requestHandler = self.httpRequestHandler
    def stop() = self.applicationLifecycle.stop()
  }

  // Don't use uk.gov.hmrc.play.graphite.GraphiteMetricsImpl as it won't allow hot reload due to overridden onStop() method
  lazy val metrics = new MetricsImpl(applicationLifecycle, configuration)

  override lazy val httpFilters: Seq[EssentialFilter] = new FFilters(configuration, metrics, csrfFilter, auditConnector, appName)(materializer).filters

  // We need to create explicit AdminController and provide it into injector so Runtime DI could be able
  // to find it when endpoints in health.Routes are being called
  lazy val adminController = new AdminController(configuration)

  lazy val templateController = new _root_.controllers.template.Template(httpErrorHandler)

  lazy val customInjector: Injector = new SimpleInjector(injector) + adminController + templateController + wsApi + messagesApi

  lazy val healthRoutes: health.Routes = health.Routes

  lazy val templateRoutes: template.Routes = template.Routes

  lazy val metricsController = new MetricsController(metrics)

  val eeittUrl: String = s"${baseUrl("eeitt")}/eeitt"

  val authConnector = new FrontendAuthConnector(configuration, environment.mode)
  val eeittConnector = new EeittConnector(eeittUrl)

  val securedActions = new SecuredActionsImpl(configuration, authConnector)

  val enrollmentVerificationController = new EnrollmentVerificationController(authConnector, eeittConnector, messagesApi, securedActions)(appConfig)
  val etmpDataLoaderProxy = new EtmpDataLoaderProxy(eeittConnector, securedActions)
  val signOutController = new SignOutController()

  val appRoutes = new _root_.app.Routes(httpErrorHandler, enrollmentVerificationController, etmpDataLoaderProxy)

  val prodRoutes = new prod.Routes(httpErrorHandler, appRoutes, healthRoutes, templateRoutes, metricsController)

  val testRoutes = new testOnlyDoNotUseInAppConf.Routes(httpErrorHandler, signOutController, prodRoutes)

  val testOnly = configuration.getString("application.router").contains("testOnlyDoNotUseInAppConf.Routes")

  override lazy val router: Router = if (testOnly) testRoutes else prodRoutes

  object ControllerConfiguration extends ControllerConfig {
    lazy val controllerConfigs = configuration.underlying.as[Config]("controllers")
  }
}
