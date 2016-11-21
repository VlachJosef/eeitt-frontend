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

package uk.gov.hmrc.eeitt.connectors

import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.{ WS, WSResponse }
import uk.gov.hmrc.eeitt.WSHttp
import uk.gov.hmrc.eeitt.models.{ AgentEnrollmentDetails, EnrollmentDetails }
import uk.gov.hmrc.eeitt.utils.FuturesLogging.withLoggingFutures
import uk.gov.hmrc.play.config.ServicesConfig

case class VerificationResult(error: Option[String])

object VerificationResult {
  implicit val formats = Json.format[VerificationResult]
}

import uk.gov.hmrc.play.http.{ HeaderCarrier, HttpPost }

import scala.concurrent.{ ExecutionContext, Future }

trait EeittConnector {
  def httpPost: HttpPost

  def eeittUrl: String

  def registerNonAgent(enrollmentDetails: EnrollmentDetails)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[VerificationResult] = {
    withLoggingFutures {
      httpPost.POST[EnrollmentDetails, VerificationResult](eeittUrl + "register", enrollmentDetails)
    }
  }

  def registerAgent(agentEnrollmentDetails: AgentEnrollmentDetails)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[VerificationResult] = {
    withLoggingFutures {
      httpPost.POST[AgentEnrollmentDetails, VerificationResult](eeittUrl + "register-agent", agentEnrollmentDetails)
    }
  }

  def loadBusinessUsers(source: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[WSResponse] = {
    withLoggingFutures {
      WS.url(eeittUrl + "etmp-data/business-users").post(source)
    }
  }

  def loadAgents(source: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[WSResponse] = {
    withLoggingFutures {
      WS.url(eeittUrl + "etmp-data/agents").post(source)
    }
  }
}

object EeittConnector extends EeittConnector with ServicesConfig {
  lazy val httpPost = WSHttp
  def eeittUrl: String = s"${baseUrl("eeitt")}/eeitt/"
}
