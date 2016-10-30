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

import play.api.libs.json.Json
import uk.gov.hmrc.eeitt.WSHttp
import uk.gov.hmrc.eeitt.models.{AgentEnrollmentDetails, EnrollmentDetails}
import uk.gov.hmrc.play.config.ServicesConfig

case class VerificationResult(error: Option[String])

object VerificationResult {
  implicit val formats = Json.format[VerificationResult]
}

import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost}

import scala.concurrent.{ExecutionContext, Future}

trait EeittConnector {
  def httpPost: HttpPost

  def eeittUrl: String

  def registerNonAgent(enrollmentDetails: EnrollmentDetails)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[VerificationResult] = {
    httpPost.POST[EnrollmentDetails, VerificationResult](eeittUrl, enrollmentDetails)
  }

  def registerAgent(agentEnrollmentDetails: AgentEnrollmentDetails)
                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[VerificationResult] = {
    httpPost.POST[AgentEnrollmentDetails, VerificationResult](eeittUrl, agentEnrollmentDetails)
  }
}

object EeittConnector extends EeittConnector with ServicesConfig {
  lazy val httpPost = WSHttp

  def eeittUrl: String = s"${baseUrl("eeitt")}/eeitt/verify"

  // todo: remove mocked call once back-end is ready
  override def registerNonAgent(enrollmentDetails: EnrollmentDetails)
                               (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[VerificationResult] = {
    if (enrollmentDetails.registrationNumber == "foo") {
      Future.successful(VerificationResult(None))
    } else {
      Future.successful(VerificationResult(Some("For testing the only allowed registration number is 'foo'")))
    }
  }

  // todo: remove mocked call once back-end is ready
  override def registerAgent(agentEnrollmentDetails: AgentEnrollmentDetails)
                            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[VerificationResult] = {
    if (agentEnrollmentDetails.arn == "foo") {
      Future.successful(VerificationResult(None))
    } else {
      Future.successful(VerificationResult(Some("For testing the only allowed ARN number is 'foo'")))
    }
  }
}


