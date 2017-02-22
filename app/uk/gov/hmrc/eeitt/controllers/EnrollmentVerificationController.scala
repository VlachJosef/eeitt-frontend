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

package uk.gov.hmrc.eeitt.controllers

import play.api.Configuration
import play.api.mvc.Action
import uk.gov.hmrc.eeitt.{ AppConfig, FrontendAuthConnector }
import uk.gov.hmrc.eeitt.connectors.{ EeittConnector, VerificationResult }
import uk.gov.hmrc.eeitt.models._
import uk.gov.hmrc.eeitt.views.html._
import uk.gov.hmrc.play.frontend.auth.Actions
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.eeitt.controllers.auth.SecuredActions
import play.api.i18n.{ Messages, MessagesApi, I18nSupport }

import scala.concurrent.Future

class EnrollmentVerificationController(
    val authConnector: AuthConnector,
    eeittConnector: EeittConnector,
    val messagesApi: MessagesApi,
    sa: SecuredActions
)(implicit appConfig: AppConfig) extends FrontendController with Actions with I18nSupport {

  def displayVerificationPage(callbackUrl: String) = {
    sa.AsyncAuthenticatedAction { implicit authContext => implicit request =>
      authConnector.getUserDetails[UserDetails](authContext).map {
        case UserDetails(NonAgent, groupIdentifier) =>
          Ok(verification_non_agent(EnrollmentDetails.form, callbackUrl, groupIdentifier))

        case UserDetails(Agent, groupIdentifier) =>
          Ok(verification_agent(AgentEnrollmentDetails.form, callbackUrl, groupIdentifier))
      }
    }
    /* Action.async { implicit request =>
     *   //println("configuration.underlying.root().render() " + configuration.underlying.root().render())
     *   request.cookies.foreach(println)
     *
     *   import uk.gov.hmrc.play.http.HeaderNames.{ xRequestId, xRequestTimestamp }
     *
     *   println("play.api.mvc.Session.cookieSigner " + play.api.mvc.Session.cookieSigner.getClass.getCanonicalName)
     *   val cookieOpt = request.cookies.get(play.api.mvc.Session.COOKIE_NAME)
     *   println("play.api.mvc.Session.COOKIE_NAME " + play.api.mvc.Session.COOKIE_NAME)
     *   println("cookieOpt.get.name               " + cookieOpt.map(_.name).getOrElse("NONAME"))
     *   println("cookies.get(Session.COOKIE_NAME) " + request.cookies.get(play.api.mvc.Session.COOKIE_NAME))
     *
     *   val ddd = EnrollmentDetails(registrationNumber = "12", postcode = None, groupId = "dasdasd")
     *   println("PPPPPPPPPPPP request PPPPPPPPPPPPPPPPPPPP " + request.session)
     *   eeittConnector.registerNonAgent(ddd).map(_ => Ok("hello" + request.headers.get(xRequestId)))
     *
     * } */
  }

  def submitEnrollmentDetails(callbackUrl: String) = sa.AsyncAuthenticatedAction { implicit authContext => implicit request =>
    authConnector.getUserDetails[UserDetails](authContext).flatMap {
      case UserDetails(_, groupIdentifier) =>
        EnrollmentDetails.form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(
              BadRequest(verification_non_agent(formWithErrors, callbackUrl, groupIdentifier))
            ),
          formData =>
            eeittConnector.registerNonAgent(formData).map {
              case VerificationResult(Some(errorMsg)) =>
                val formWithErrors = EnrollmentDetails.form.withGlobalError(errorMsg)
                BadRequest(verification_non_agent(formWithErrors, callbackUrl, groupIdentifier))
              case VerificationResult(noErrors) =>
                Redirect(callbackUrl)
            }
        )
    }
  }

  def submitAgentEnrollmentDetails(callbackUrl: String) = sa.AsyncAuthenticatedAction { implicit authContext => implicit request =>
    authConnector.getUserDetails[UserDetails](authContext).flatMap {
      case UserDetails(_, groupIdentifier) =>
        AgentEnrollmentDetails.form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(
              BadRequest(verification_agent(formWithErrors, callbackUrl, groupIdentifier))
            ),
          formData =>
            eeittConnector.registerAgent(formData).map {
              case VerificationResult(Some(errorMsg)) =>
                val formWithErrors = AgentEnrollmentDetails.form.withGlobalError(errorMsg)
                BadRequest(verification_agent(formWithErrors, callbackUrl, groupIdentifier))
              case VerificationResult(noErrors) =>
                Redirect(callbackUrl)
            }
        )
    }
  }

}
