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

import java.io.File
import org.scalatest.TestSuite
import org.scalatestplus.play.{ BaseOneAppPerSuite, FakeApplicationFactory }
import play.api.Configuration
import play.api.http.Status
import play.api.libs.json.JsString
import play.api.{ Environment, Mode }
import scala.concurrent.{ ExecutionContext, Future }
import uk.gov.hmrc.eeitt.connectors.{ EeittConnector, VerificationResult }
import uk.gov.hmrc.eeitt.models.{ AgentEnrollmentDetails, EnrollmentDetails, ImportMode, UserMode }
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.{ HeaderCarrier, HttpGet, HttpReads, HttpResponse }
import uk.gov.hmrc.eeitt.models._

trait ApplicationComponentsOnePerSuite extends BaseOneAppPerSuite with FakeApplicationFactory {
  this: TestSuite =>

  def additionalConfiguration: Map[String, Any] = Map.empty[String, Any]

  private lazy val config = Configuration.from(additionalConfiguration)

  override lazy val fakeApplication =
    new ApplicationLoader().load(context.copy(initialConfiguration = context.initialConfiguration ++ config))

  def context: play.api.ApplicationLoader.Context = {
    val classLoader = play.api.ApplicationLoader.getClass.getClassLoader
    val env = new Environment(new File("."), classLoader, Mode.Test)
    play.api.ApplicationLoader.createContext(env)
  }
}

trait FakeEeittConnector {
  def eeittConnector(verificationResult: VerificationResult = null): EeittConnector = new EeittConnector("hello") {
    def eeittUrl: String = "hello eeitt"

    override def registerNonAgent(enrollmentDetails: EnrollmentDetails)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[VerificationResult] = {
      Future.successful(verificationResult)
    }
    override def registerAgent(agentEnrollmentDetails: AgentEnrollmentDetails)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[VerificationResult] = {
      Future.successful(verificationResult)
    }
    override def load(source: String, importMode: ImportMode, userMode: UserMode)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
      Future.successful(HttpResponse(Status.CREATED, Some(JsString(""))))
    }
  }
}

trait FakeAuthConnector {
  def authConnector(user: AuthContext): AuthConnector = new AuthConnector {
    def http: HttpGet = ???
    val serviceUrl: String = "test-service-url"

    override def getUserDetails[T](authContext: AuthContext)(implicit hc: HeaderCarrier, reads: HttpReads[T]): Future[T] = {
      val affinityGroup = if (user.isDelegating) Agent else NonAgent
      Future.successful(UserDetails(affinityGroup, "test-group-id")).asInstanceOf[Future[T]]
    }
  }
}
