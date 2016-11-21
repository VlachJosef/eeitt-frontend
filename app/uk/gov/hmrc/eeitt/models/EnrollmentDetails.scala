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

package uk.gov.hmrc.eeitt.models

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

case class EnrollmentDetails(
  registrationNumber: String,
  postcode: Option[String],
  groupId: String
)

object EnrollmentDetails {
  implicit val formats = Json.format[EnrollmentDetails]

  val form = Form(mapping(
    "registrationNumber" -> text,
    "postcode" -> optional(text),
    "groupId" -> text
  )(EnrollmentDetails.apply)(EnrollmentDetails.unapply))
}

case class AgentEnrollmentDetails(
  arn: String,
  postcode: Option[String],
  groupId: String
)

object AgentEnrollmentDetails {
  implicit val formats = Json.format[AgentEnrollmentDetails]

  val form = Form(mapping(
    "arn" -> text,
    "postcode" -> optional(text),
    "groupId" -> text
  )(AgentEnrollmentDetails.apply)(AgentEnrollmentDetails.unapply))
}
