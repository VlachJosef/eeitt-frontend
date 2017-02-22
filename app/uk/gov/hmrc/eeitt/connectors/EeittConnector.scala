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

package uk.gov.hmrc.eeitt.connectors

import play.api.Application
import play.api.Play.current
import play.api.libs.json.Json
import uk.gov.hmrc.eeitt.WSHttp
import uk.gov.hmrc.eeitt.models.{ AgentEnrollmentDetails, EnrollmentDetails, ImportMode, UserMode }
import uk.gov.hmrc.eeitt.utils.FuturesLogging.withLoggingFutures
import uk.gov.hmrc.play.http.{ HeaderCarrier, HttpPost, HttpResponse }
import scala.concurrent.{ ExecutionContext, Future }

case class VerificationResult(error: Option[String])

object VerificationResult {
  implicit val formats = Json.format[VerificationResult]
}

class EeittConnector(eeittUrl: String) {
  lazy val httpPost = WSHttp

  def registerNonAgent(enrollmentDetails: EnrollmentDetails)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[VerificationResult] = {
    withLoggingFutures {
      httpPost.POST[EnrollmentDetails, VerificationResult](eeittUrl + "/register", enrollmentDetails)
    }
  }

  def registerAgent(agentEnrollmentDetails: AgentEnrollmentDetails)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[VerificationResult] = {
    withLoggingFutures {
      httpPost.POST[AgentEnrollmentDetails, VerificationResult](eeittUrl + "/register-agent", agentEnrollmentDetails)
    }
  }

  def load(source: String, importMode: ImportMode, userMode: UserMode)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    withLoggingFutures {
      httpPost.doPost(s"$eeittUrl/etmp-data/$importMode/$userMode", source, Seq.empty[(String, String)])
    }
  }
}
