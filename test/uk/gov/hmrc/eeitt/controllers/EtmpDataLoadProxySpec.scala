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

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.JsString
import play.api.{ Configuration, Environment }
import play.api.http.{ HeaderNames, Status }
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.mvc.{ Action, AnyContent, RequestHeader, Result }
import play.api.test.{ FakeHeaders, FakeRequest }
import uk.gov.hmrc.eeitt.{ ApplicationComponentsOnePerSuite, FakeEeittConnector }
import uk.gov.hmrc.eeitt.connectors.{ EeittConnector, VerificationResult }
import uk.gov.hmrc.eeitt.controllers.auth.{ SecuredActions, SecuredActionsImpl }
import uk.gov.hmrc.eeitt.infrastructure.{ BasicAuth, BasicAuthConfiguration }
import uk.gov.hmrc.eeitt.models.{ AgentEnrollmentDetails, Agents, BusinessUsers, EnrollmentDetails, ImportMode, Live, UserMode }
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.{ HeaderCarrier, HttpPost, HttpResponse }
import uk.gov.hmrc.play.test.{ UnitSpec }

import scala.concurrent.{ ExecutionContext, Future }

class EtmpDataLoadProxySpec extends UnitSpec with ApplicationComponentsOnePerSuite with ScalaFutures with FakeEeittConnector {

  override def additionalConfiguration = Map("basicAuth.whitelist" -> "192.168.1.1")

  def basic64(s: String): String = {
    BaseEncoding.base64().encode(s.getBytes(Charsets.UTF_8))
  }

  "business user data upload" should {
    "return response with FORBIDDEN status when basic auth is missing" in {

      val serverUrl = "http://test.invalid:8000"

      val fakeRequest = new FakeRequest("POST", "/eeitt-auth/etmp-data/business-users", FakeHeaders(), body = "test data") {
        override lazy val host = serverUrl
      }.withHeaders("True-Client-IP" -> "192.168.1.1")

      val result: Result = proxy.load(BusinessUsers, Live)(fakeRequest).futureValue

      result.header.status shouldBe Status.FORBIDDEN
    }
  }

  "business user data upload" should {
    "return response with FORBIDDEN status when IP address is missing" in {

      val serverUrl = "http://test.invalid:8000"

      val fakeRequest = new FakeRequest("POST", "/eeitt-auth/etmp-data/business-users", FakeHeaders(), body = "test data") {
        override lazy val host = serverUrl
      }.withHeaders(HeaderNames.AUTHORIZATION -> s"""Basic ${basic64("dave:davespassword")}""")

      val result: Result = proxy.load(BusinessUsers, Live)(fakeRequest).futureValue

      result.header.status shouldBe Status.FORBIDDEN
    }
  }

  "business user data upload" should {
    "return response with FORBIDDEN status when IP address is incorrect" in {

      val serverUrl = "http://test.invalid:8000"

      val fakeRequest = new FakeRequest("POST", "/eeitt-auth/etmp-data/business-users", FakeHeaders(), body = "test data") {
        override lazy val host = serverUrl
      }.withHeaders(
        HeaderNames.AUTHORIZATION -> s"""Basic ${basic64("dave:davespassword")}""",
        "True-Client-IP" -> "10.0.0.1"
      )

      val result: Result = proxy.load(BusinessUsers, Live)(fakeRequest).futureValue

      result.header.status shouldBe Status.FORBIDDEN
    }
  }

  "business user data upload" should {
    "return response with FORBIDDEN status when basic auth is incorrect" in {

      val serverUrl = "http://test.invalid:8000"

      val fakeRequest = new FakeRequest("POST", "/eeitt-auth/etmp-data/business-users", FakeHeaders(), body = "test data") {
        override lazy val host = serverUrl
      }.withHeaders(
        HeaderNames.AUTHORIZATION -> s"""Basic ${basic64("dave:notthepassword")}""",
        "True-Client-IP" -> "192.168.1.1"
      )

      val result: Result = proxy.load(BusinessUsers, Live)(fakeRequest).futureValue

      result.header.status shouldBe Status.FORBIDDEN
    }
  }

  "business user data upload" should {
    "return response with CREATED status from the proxy when basic auth is present" in {

      val serverUrl = "http://test.invalid:8000"

      val fakeRequest = new FakeRequest("POST", "/eeitt-auth/etmp-data/business-users", FakeHeaders(), body = "test data") {
        override lazy val host = serverUrl
      }.withHeaders(
        HeaderNames.AUTHORIZATION -> s"""Basic ${basic64("dave:davespassword")}""",
        "True-Client-IP" -> "192.168.1.1"
      )

      val result: Result = proxy.load(BusinessUsers, Live)(fakeRequest).futureValue

      result.header.status shouldBe Status.CREATED
    }
  }

  "agent data upload" should {
    "return response with FORBIDDEN status when basic auth is missing" in {

      val serverUrl = "http://test.invalid:8000"

      val fakeRequest = new FakeRequest("POST", "/eeitt-auth/etmp-data/agents", FakeHeaders(), body = "test data") {
        override lazy val host = serverUrl
      }.withHeaders(("True-Client-IP" -> "192.168.1.1"))

      val result: Result = proxy.load(Agents, Live)(fakeRequest).futureValue

      result.header.status shouldBe Status.FORBIDDEN
    }
  }

  "agent data upload" should {
    "return response with CREATED status from the proxy when basic auth is present" in {

      val serverUrl = "http://test.invalid:8000"

      val fakeRequest = new FakeRequest("POST", "/eeitt-auth/etmp-data/agents", FakeHeaders(), body = "agent test data") {
        override lazy val host = serverUrl
      }.withHeaders(
        HeaderNames.AUTHORIZATION -> s"""Basic ${basic64("dave:davespassword")}""",
        "True-Client-IP" -> "192.168.1.1"
      )

      val result: Result = proxy.load(Agents, Live)(fakeRequest).futureValue

      result.header.status shouldBe Status.CREATED
    }
  }

  val securedActions = new SecuredActionsImpl(fakeApplication.configuration, null)

  val proxy = new EtmpDataLoaderProxy(eeittConnector(), securedActions)

}
