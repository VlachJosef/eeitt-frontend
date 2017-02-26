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

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Millis, Seconds, Span }
import play.api.{ Configuration, Environment, Mode }
import play.api.i18n.{ DefaultLangs, DefaultMessagesApi }
import play.api.mvc.{ Action, AnyContent, RequestHeader, Result }
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.filters.csrf.CSRF.Token.{ RequestTag, NameRequestTag }
import uk.gov.hmrc.eeitt.{ AppConfig, ApplicationComponentsOnePerTest, FakeAuthConnector, FakeEeittConnector }
import uk.gov.hmrc.eeitt.connectors.VerificationResult
import uk.gov.hmrc.eeitt.controllers.auth.{ SecuredActions, TestUsers }
import uk.gov.hmrc.eeitt.models._
import uk.gov.hmrc.play.frontend.auth._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.test.UnitSpec
import scala.concurrent.Future

class EnrollmentVerificationControllerSpec extends UnitSpec with ScalaFutures with ApplicationComponentsOnePerTest with TestUsers with FakeEeittConnector with FakeAuthConnector {

  val fakeRequestWithCsrfToken = FakeRequest().copyFakeRequest(tags = Map(
    NameRequestTag -> "",
    RequestTag -> ""
  ))

  "Verification page" should {
    "be specific for an agent if agent has logged-in" in {
      val controller = enrollmentVerificationController(agentUser)

      val result = controller.displayVerificationPage(callbackUrl = "/dfs/forms")(fakeRequestWithCsrfToken).futureValue

      status(result) shouldBe 200
      contentAsString(result) should include("Access code")
    }
    "be specific for a business user if non-agent user has logged-in" in {
      val controller = enrollmentVerificationController(businessUser)

      val result = controller.displayVerificationPage(callbackUrl = "/dfs/forms")(fakeRequestWithCsrfToken).futureValue

      status(result) shouldBe 200
      contentAsString(result) should include("Registration Number")
    }
  }

  "Submitting enrollment details for a business user" should {
    "redirect to the eventual callbackUrl when data was correct" in {
      val successfulVerificationOfFormData = VerificationResult(None)
      val controller = enrollmentVerificationController(businessUser, successfulVerificationOfFormData)
      val request = FakeRequest().withFormUrlEncodedBody(
        "registrationNumber" -> "foo",
        "groupId" -> "test-group-id",
        "postcode" -> "postcode"
      )
      val callbackUrl = "/form/inside/dfs"

      val result = controller.submitEnrollmentDetails(callbackUrl)(request).futureValue

      status(result) shouldBe 303
      redirectLocation(result).get shouldBe callbackUrl
    }
    "return 400 bad request and the form with errors when submitted data was rejected by EEITT microservice" in {
      val failedVerificationOfFormData = VerificationResult(Some("error happened"))
      val controller = enrollmentVerificationController(businessUser, failedVerificationOfFormData)
      val request = fakeRequestWithCsrfToken.withFormUrlEncodedBody(
        "registrationNumber" -> "foo",
        "groupId" -> "test-group-id",
        "postcode" -> "postcode"
      )
      val result = controller.submitEnrollmentDetails("/form/inside/dfs")(request).futureValue

      status(result) shouldBe 400
      contentAsString(result) should include("error happened")
    }
  }

  "Submitting enrollment details for an agent" should {
    "redirect to the eventual callbackUrl when data was correct" in {
      val successfulVerificationOfFormData = VerificationResult(None)
      val controller = enrollmentVerificationController(agentUser, successfulVerificationOfFormData)
      val request = FakeRequest().withFormUrlEncodedBody(
        "arn" -> "foo",
        "groupId" -> "test-group-id",
        "postcode" -> "postcode"
      )
      val callbackUrl = "/form/inside/dfs"

      val result = controller.submitAgentEnrollmentDetails(callbackUrl)(request).futureValue

      status(result) shouldBe 303
      redirectLocation(result).get shouldBe callbackUrl
    }
    "return 400 bad request and the form with errors when submitted data was rejected by EEITT microservice" in {
      val failedVerificationOfFormData = VerificationResult(Some("error happened"))
      val controller = enrollmentVerificationController(agentUser, failedVerificationOfFormData)
      val request = fakeRequestWithCsrfToken.withFormUrlEncodedBody(
        "arn" -> "foo",
        "groupId" -> "test-group-id",
        "postcode" -> "postcode"
      )
      val result = controller.submitAgentEnrollmentDetails("/form/inside/dfs")(request).futureValue

      status(result) shouldBe 400
      contentAsString(result) should include("error happened")
    }
  }

  class SecuredActionsTest(authContext: AuthContext, val authConnector: AuthConnector) extends SecuredActions {

    def AuthenticatedAction(r: UserRequest): Action[AnyContent] = Action {
      r(authContext)
    }
    def AsyncAuthenticatedAction(r: AsyncUserRequest): Action[AnyContent] = Action.async {
      r(authContext)
    }
    def BasicAuthentication(r: => Future[Result])(implicit request: RequestHeader): Future[Result] = ???
  }

  val configuration = Configuration.reference
  val mode: Mode.Mode = Mode.Test
  val env = Environment.simple(mode = mode)
  val langs = new DefaultLangs(configuration)
  val messageApi = new DefaultMessagesApi(env, configuration, langs)

  val appConfig = new AppConfig {
    val analyticsToken: String = ""
    val analyticsHost: String = ""
    val reportAProblemPartialUrl: String = ""
    val reportAProblemNonJSUrl: String = ""
  }

  def enrollmentVerificationController(user: AuthContext, verificationResult: VerificationResult = null) = {
    val authCon = authConnector(user)
    val securedActions = new SecuredActionsTest(user, authCon)
    new EnrollmentVerificationController(authCon, eeittConnector(verificationResult), messageApi, securedActions)(appConfig)
  }

  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds), interval = Span(10, Millis))

}
