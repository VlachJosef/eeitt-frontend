/*
 * Copyright 2016 HM Revenue & Customs
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

import uk.gov.hmrc.eeitt.FrontendAuthConnector
import uk.gov.hmrc.eeitt.connectors.{EeittConnector, VerificationResult}
import uk.gov.hmrc.eeitt.controllers.auth.EeittAuth
import uk.gov.hmrc.eeitt.models._
import uk.gov.hmrc.eeitt.views.html._
import uk.gov.hmrc.play.frontend.auth.Actions
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

trait EnrollmentVerificationController extends FrontendController with Actions { self: EeittAuth =>

  def eeittConnector: EeittConnector

  def displayVerificationPage(callbackUrl: String) = AsyncAuthenticatedAction {
    implicit authContext => implicit request =>
      authConnector.getUserDetails[UserDetails](authContext).map {
        case UserDetails(NonAgent, groupIdentifier) =>
          Ok(verification_non_agent(EnrollmentDetails.form, callbackUrl))

        case UserDetails(Agent, groupIdentifier) =>
          Ok(verification_agent(AgentEnrollmentDetails.form, callbackUrl))
      }
  }

  def submitEnrollmentDetails(callbackUrl: String) = AsyncAuthenticatedAction {
    implicit authContext => implicit request =>
      EnrollmentDetails.form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(
            BadRequest(verification_non_agent(formWithErrors, callbackUrl))
          ),
        formData =>
          eeittConnector.registerNonAgent(formData).map {
            case VerificationResult(Some(errorMsg)) =>
              val formWithErrors = EnrollmentDetails.form.withGlobalError(errorMsg)
              BadRequest(verification_non_agent(formWithErrors, callbackUrl))
            case VerificationResult(noErrors) =>
              Redirect(callbackUrl)
          }
      )
  }

  def submitAgentEnrollmentDetails(callbackUrl: String) = AsyncAuthenticatedAction {
    implicit authContext => implicit request =>
      AgentEnrollmentDetails.form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(
            BadRequest(verification_agent(formWithErrors, callbackUrl))
          ),
        formData =>
          eeittConnector.registerAgent(formData).map {
            case VerificationResult(Some(errorMsg)) =>
              val formWithErrors = EnrollmentDetails.form.withGlobalError(errorMsg)
              BadRequest(verification_non_agent(formWithErrors, callbackUrl))
            case VerificationResult(noErrors) =>
              Redirect(callbackUrl)
          }
      )
  }

}

object EnrollmentVerificationController extends EnrollmentVerificationController with EeittAuth {
  protected def authConnector: AuthConnector = FrontendAuthConnector

  def eeittConnector: EeittConnector = EeittConnector
}