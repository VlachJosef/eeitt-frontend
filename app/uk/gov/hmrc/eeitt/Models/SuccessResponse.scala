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

package uk.gov.hmrc.eeitt.Models
import play.api.libs.json._
import play.api.libs.functional.syntax._
/**
  * Created by harrison on 16/09/16.
  */
case class Individual(firstName: String, middleName: Option[String], lastName: String, dateOfBirth: String)

object Individual {
  implicit val IndividualReads = Json.reads[Individual]
  //    (JsPath \ "firstName").read[String] and
  //      (JsPath \ "middleName").read[Option[String]] and
  //      (JsPath \ "lastName").read[String] and
  //      (JsPath \ "dateOfBirth").read[String]
  //    ) (Individual.apply _)
}


case class Organisation(organisationName: String, isAGroup: Option[Boolean], organisationType: Option[String])

object Organisation {
  implicit val OrganisationRead = Json.reads[Organisation]
  //    (JsPath \ "organisationName").read[String] and
  //      (JsPath \ "isAGroup").read[Option[Boolean]] and
  //      (JsPath \ "organisationType").read[Option[String]]
  //    ) (Organisation.apply _)
}


case class AddressDetails(addressLine1: String,
                          addressLine2: String,
                          addressLine3: Option[String],
                          addressLine4: Option[String],
                          postalCode: Option[String],
                          countryCode: String)

object AddressDetails {

  implicit val AddressDetailsRead = Json.reads[AddressDetails]

  //    (JsPath \ "addressLine1").read[String] and
  //      (JsPath \ "addressLine2").read[String] and
  //      (JsPath \ "addressLine3").read[Option[String]] and
  //      (JsPath \ "addressLine4").read[Option[String]] and
  //      (JsPath \ "postalCode").read[Option[String]] and
  //      (JsPath \ "countryCode").read[String]
  //    ) (AddressDetails.apply _)
}

case class ContactDetails(telephone: Option[String] = None,
                          mobileNumber: Option[String] = None,
                          faxNumber: Option[String] = None,
                          eMailAddress: Option[String] = None)

object ContactDetails {
  /*  implicit val ContactDetailsReads: Reads[ContactDetails] = (
  (JsPath \ "telephone").read[Option[String]] and
    (JsPath \ "mobileNumber").read[Option[String]] and
    (JsPath \ "faxNumber").readNullable[String] and
    (JsPath \ "eMailAddress").read[Option[String]]
  ) (ContactDetails.apply _)*/

  implicit val ContactDetailsRead = Json.reads[ContactDetails]


}
case class SuccessResponse(sapNumber: String,
                           safeId: String,
                           agentReferenceNumber: Option[String],
                           isEditable: Boolean,
                           isAnAgent: Boolean,
                           isAnIndividual: Boolean,
                           individual: Option[Individual],
                           organisation: Option[Organisation],
                           addressDetails: AddressDetails,
                           contactDetails: ContactDetails)

object SuccessResponse {
  implicit val SuccessResponseReads = Json.reads[SuccessResponse]
  //    (JsPath \ "sapNumber").read[String] and
  //      (JsPath \ "safeId").read[String] and
  //      (JsPath \ "agentReferenceNumber").read[Option[String]] and
  //      (JsPath \ "isEditable").read[Boolean] and
  //      (JsPath \ "isAnAgent").read[Boolean] and
  //      (JsPath \ "isAnIndividual").read[Boolean] and
  //      (JsPath \ "individual").read[Option[Individual]] and
  //      (JsPath \ "organisation").read[Option[Organisation]] and
  //      (JsPath \ "addressDetails").read[AddressDetails] and
  //      (JsPath \ "contactDetails").read[ContactDetails]
  //    ) (SuccessResponse.apply _)
}



