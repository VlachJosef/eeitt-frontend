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

import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{eq => sameAs}
import org.mockito.Mockito._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mock.MockitoSugar
import play.api.LoggerLike
import play.api.http.Status


import play.twirl.api.Html
import uk.gov.hmrc.eeitt.DfsConnector
import uk.gov.hmrc.eeitt.utils.utils.DfsFrontEndServer
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.partials.HeaderCarrierForPartials
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

/**
  * Created by harrison on 03/10/16.
  */
class DFSConnectorTest extends UnitSpec
  with WithFakeApplication
  with ScalaFutures
  with DfsFrontEndServer
  with IntegrationPatience
  with MockitoSugar {


  testSuccessfulCall
  testFailedCalls


  def testSuccessfulCall = {

      "POST a properly formatted payload" in new TestCase {
        givenDfsRespondsWith(
          requestBody = expectedRequestBody,
          responseBody = expectedResponseBody
        )

        implicit val hc = HeaderCarrierForPartials(HeaderCarrier(), "")
        val response: Html = connector.renderForm(
          returnUrl = returnUrl,
          utr = utr
        ).futureValue

        response shouldBe Html(expectedResponseBody)
      }

  }

  def testFailedCalls = {
    val allErrors = createHttp500Errors.toSeq ++ createHttp400Errors.toSeq ++ createHttp300Errors.toSeq
    allErrors.foreach { case (key, value) =>
      testFailedCall(key, value)
    }
  }

  def testFailedCall(key:String, value:Int) = {
    "Display Http error message " + key in new TestCase {

      givenDfsRespondsWith(
        requestBody = expectedRequestBody,
        responseBody = expectedResponseBody,
        status = value
      )
      implicit val hc = HeaderCarrierForPartials(HeaderCarrier(), "")
      val response = connector.renderForm(
        returnUrl = returnUrl,
        utr = utr
      ).futureValue.toString

      response should include("id=\"errorMessage\"")
      response should include("Something went wrong. Please try again.")
    }
  }

  def createHttp500Errors: Map[String, Int] = {
    Map("INTERNAL_SERVER_ERROR" -> 500, "NOT_IMPLEMENTED" -> 501, "BAD_GATEWAY" -> 502, "GATEWAY_TIMEOUT" -> 504, "HTTP_VERSION_NOT_SUPPORTED" -> 505, "INSUFFICIENT_STORAGE" -> 507)
  }

  def createHttp400Errors: Map[String, Int] = {
    Map("BAD_REQUEST" -> 400, "UNAUTHORIZED" -> 401, "PAYMENT_REQUIRED" -> 402, "NOT_FOUND" -> 404, "METHOD_NOT_ALLOWED" -> 405, "PROXY_AUTHENTICATION_REQUIRED" -> 407)
  }

  def createHttp300Errors: Map[String, Int] = {
    Map("MULTIPLE_CHOICES" -> 300, "MOVED_PERMANENTLY" -> 301, "FOUND" -> 302, "NOT_MODIFIED" -> 304, "USE_PROXY" -> 305, "TEMPORARY_REDIRECT" -> 307)
  }

  def createHttp200Errors: Map[String, Int] = {
    Map("OK" -> 200, "CREATED" -> 201, "ACCEPTED" -> 202, "NON_AUTHORITATIVE_INFORMATION" -> 203, "NO_CONTENT" -> 204, "RESET_CONTENT" -> 206)
  }

  trait TestCase {
    val forType = "landfill-tax-group-member-details"
    val connector = new DfsConnector {
     override val logger = mock[LoggerLike]
      override def http = DfsConnector.http
      override lazy val url: String = {
        val formTypeRef: String = "landfill-tax-group-member-details"
        s"http://localhost:9090/forms/form/$formTypeRef/new"
      }
    }

    val returnUrl = "/personal-account/messages/[encrypted-stuff]"
    val utr = "utr-123"


    val expectedRequestBody =
      s"""
         |{
         |   "displayFormAsPartial": true,
         |   "saveEnabled": true,
         |   "afterSubmissionURI": "$returnUrl",
         |   "secureMailboxURL": ""
         | }
       """.stripMargin

    val expectedResponseBody = "<html></html>"
  }

}
