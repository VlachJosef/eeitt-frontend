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
import org.scalatestplus.play.OneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.eeitt.connectors.{ EeittConnector, VerificationResult }
import uk.gov.hmrc.eeitt.controllers.auth.{ TestEeittAuth, TestUsers }
import uk.gov.hmrc.eeitt.models._
import uk.gov.hmrc.play.frontend.auth._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.{ HeaderCarrier, HttpGet, HttpPost, HttpReads }
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.{ ExecutionContext, Future }

class EnrollmentVerificationControllerSpec extends UnitSpec with ScalaFutures with OneAppPerSuite with TestUsers {

  "Verification page" should {
    "be specific for an agent if agent has logged-in" in {
      val controller = enrollmentVerificationController(agentUser)

      val result = controller.displayVerificationPage(callbackUrl = "/dfs/forms")(FakeRequest()).futureValue

      status(result) shouldBe 200
      contentAsString(result) should include("Agent Reference Number")
    }
    "be specific for a business user if non-agent user has logged-in" in {
      val controller = enrollmentVerificationController(businessUser)

      val result = controller.displayVerificationPage(callbackUrl = "/dfs/forms")(FakeRequest()).futureValue

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
      val request = FakeRequest().withFormUrlEncodedBody(
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
      val request = FakeRequest().withFormUrlEncodedBody(
        "arn" -> "foo",
        "groupId" -> "test-group-id",
        "postcode" -> "postcode"
      )
      val result = controller.submitAgentEnrollmentDetails("/form/inside/dfs")(request).futureValue

      status(result) shouldBe 400
      contentAsString(result) should include("error happened")
    }
  }

  def enrollmentVerificationController(user: AuthContext, verificationResult: VerificationResult = null) =
    new EnrollmentVerificationController with TestEeittAuth {

      def eeittConnector: EeittConnector = new EeittConnector {
        def eeittUrl: String = ???

        def httpPost: HttpPost = ???

        override def registerNonAgent(enrollmentDetails: EnrollmentDetails)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[VerificationResult] = {
          Future.successful(verificationResult)
        }
        override def registerAgent(agentEnrollmentDetails: AgentEnrollmentDetails)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[VerificationResult] = {
          Future.successful(verificationResult)
        }
      }

      protected def authConnector: AuthConnector = new AuthConnector {
        def http: HttpGet = ???
        val serviceUrl: String = "test-service-url"

        override def getUserDetails[T](authContext: AuthContext)(implicit hc: HeaderCarrier, reads: HttpReads[T]): Future[T] = {
          val affinityGroup = if (user.isDelegating) Agent else NonAgent
          Future.successful(UserDetails(affinityGroup, "test-group-id")).asInstanceOf[Future[T]]
        }
      }

      def authContext: AuthContext = user
    }

  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds), interval = Span(10, Millis))

}
