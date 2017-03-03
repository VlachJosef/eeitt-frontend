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

package uk.gov.hmrc.eeitt.controllers.auth

import play.api.mvc.RequestHeader
import play.api.{ Configuration, Logger }
import play.api.mvc.Action
import play.api.mvc.Results.Redirect
import play.api.mvc.{ AnyContent, Request, Result }
import scala.concurrent.Future
import scala.util.Try
import uk.gov.hmrc.eeitt.infrastructure.{ Address, BasicAuth, BasicAuthConfiguration, BasicAuthDisabled, BasicAuthEnabled, User }
import uk.gov.hmrc.eeitt.controllers.{ AsyncUserRequest, UserRequest }
import uk.gov.hmrc.play.frontend.auth._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

trait SecuredActions extends Actions {
  def AuthenticatedAction(r: UserRequest): Action[AnyContent]
  def AsyncAuthenticatedAction(r: AsyncUserRequest): Action[AnyContent]
  def BasicAuthentication(r: => Future[Result])(implicit request: RequestHeader): Future[Result]
}

class SecuredActionsImpl(configuration: Configuration, val authConnector: AuthConnector) extends SecuredActions {

  private val authenticatedBy = AuthenticatedBy(new EeittAuthenticationProvider(configuration), EeittPageVisibilityPredicate)

  override def AuthenticatedAction(r: UserRequest) = authenticatedBy(r)

  override def AsyncAuthenticatedAction(r: AsyncUserRequest) = authenticatedBy.async(r)

  override def BasicAuthentication(r: => Future[Result])(implicit request: RequestHeader) = BasicAuth(BasicAuthConf(configuration))(r)
}

object BasicAuthConf {
  def apply(config: Configuration): BasicAuthConfiguration = {
    def getUsers(config: Configuration): List[User] = {
      config.getString("basicAuth.authorizedUsers").map { s =>
        s.split(";").flatMap(
          user => {
            user.split(":") match {
              case Array(username, password) => Some(User(username, password))
              case _ => {
                Logger.warn("A user:password value has been malformed in basicAuth.authorizedUsers, ignoring it")
                None
              }
            }
          }
        ).toList
      }.getOrElse(List.empty)
    }

    def getWhitelist(config: Configuration): Option[List[Address]] = {

      val whitelist = config.getString("basicAuth.whitelist")
      config.getString("basicAuth.whitelist").map {
        _.split(",").map(a => Address(a)).toList
      } match {
        case None =>
          Logger.warn("Configuration of basicAuth.whitelist has not been provided, so no whitelisting of IP addresses for BasicAuth access")
          None
        case Some(x) =>
          Logger.info(s""""Whitelisting of IP addresses for BasicAuth access configured to [${x.map(_.ip).mkString(",")}]""")
          Some(x)
      }
    }

    config.getString("feature.basicAuthEnabled")
      .flatMap(flag => Try(flag.toBoolean).toOption) match {
        case Some(true) => BasicAuthEnabled(getUsers(config), getWhitelist(config))
        case Some(false) => BasicAuthDisabled
        case _ => {
          Logger.warn("A boolean configuration value has not been provided for feature.basicAuthEnabled, defaulting to false")
          BasicAuthDisabled
        }
      }
  }
}

class EeittAuthenticationProvider(configuration: Configuration) extends GovernmentGateway {

  val eeittFrontendBaseUrl = configuration.getString("eeitt-frontend-base-url").getOrElse("")
  val governmentGatewaySignInUrl = configuration.getString("government-gateway-sign-in-url").getOrElse("")

  override def redirectToLogin(implicit request: Request[_]): Future[Result] = {

    val queryStringParams = Map("continue" -> Seq(eeittFrontendBaseUrl + request.uri))
    Future.successful(Redirect(loginURL, queryStringParams))
  }

  def continueURL: String = "not used since we override redirectToLogin"

  def loginURL: String = governmentGatewaySignInUrl
}

object EeittPageVisibilityPredicate extends PageVisibilityPredicate {
  def apply(authContext: AuthContext, request: Request[AnyContent]): Future[PageVisibilityResult] =
    Future.successful(PageIsVisible)
}

