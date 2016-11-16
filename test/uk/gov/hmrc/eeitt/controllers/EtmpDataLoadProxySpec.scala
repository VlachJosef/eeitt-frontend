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

import com.ning.http.client.{FluentCaseInsensitiveStringsMap, Response}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.tools.Durations
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws.ning.NingWSResponse
import play.api.libs.ws.{WS, WSRequestHolder, WSResponse}
import play.api.mvc.Result
import play.api.test.{FakeHeaders, FakeRequest}
import uk.gov.hmrc.eeitt.connectors.{EeittConnector, VerificationResult}
import uk.gov.hmrc.eeitt.testonly.EtmpDataLoaderProxy
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.mvc.Action
import sun.font.CreatedFontTracker

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class EtmpDataLoadProxySpec extends UnitSpec with WithFakeApplication with ScalaFutures {
  "business user data upload" should {
    "return response with FORBIDDEN status when basic auth is missing" in {
      val proxy = etmpDataLoaderProxy()

      val serverUrl = "http://test.invalid:8000"

      val fakeRequest = new FakeRequest("POST", "/eeitt-auth/test-only/etmp-data/business-users", FakeHeaders(), body = "test data") {
        override lazy val host = serverUrl
      }

      val result: Result = proxy.loadBusinessUsers()(fakeRequest).futureValue

      result.header.status shouldBe Status.FORBIDDEN
    }
  }

  "business user data upload" should {
    "return response with UNAUTH status when basic auth is incorrect" in {
      ???
    }
  }
  "business user data upload" should {
    "return response with OK status and proxy the request when basic auth is present" in {
      ???
    }
  }

  def etmpDataLoaderProxy() = new EtmpDataLoaderProxy with TestEeittConnector

  trait TestEeittConnector {
    def eeittConnector: EeittConnector = new EeittConnector {
      def eeittUrl: String = ???

      def httpPost: HttpPost = ???

      override def testOnlyLoadBusinessUsers(source: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[WSResponse] = {

        Future.successful(testResponse(Status.CREATED))
      }
    }

    private def testResponse(statusCode : Int) = {
      NingWSResponse(ahcResponse = new Response {
        override def getResponseBodyExcerpt(maxLength: Int, charset: String) = ???

        override def getResponseBodyExcerpt(maxLength: Int) = ???

        override def getResponseBodyAsByteBuffer = ???

        override def getStatusCode = statusCode

        override def getResponseBodyAsBytes = ???

        override def getResponseBodyAsStream = ???

        override def isRedirected = ???

        override def getCookies = ???

        override def hasResponseBody = ???

        override def getStatusText = ???

        override def getHeaders(name: String) = ???

        override def getHeaders = new FluentCaseInsensitiveStringsMap()

        override def hasResponseHeaders = ???

        override def getResponseBody(charset: String) = ""

        override def getResponseBody = ???

        override def getContentType = ""

        override def hasResponseStatus = ???

        override def getUri = ???

        override def getHeader(name: String) = ???
      })
    }
  }

}