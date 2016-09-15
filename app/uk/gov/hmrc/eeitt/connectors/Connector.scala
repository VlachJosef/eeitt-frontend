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

/*

package uk.gov.hmrc.eeitt.connectors

import play.api.libs.json.JsValue
import play.libs.Json
import uk.gov.hmrc.eeitt.Models.Input
import uk.gov.hmrc.eeitt.WSHttp
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.connectors.Connector
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost}

import scala.concurrent.Future

/**
  * Created by harrison on 15/09/16.
  */
object Connector extends Connector with ServicesConfig {
  val Url = baseUrl("hello-world")
  val http = WSHttp
}

trait Connector{
  val Url: String
  val http: HttpGet with HttpPost


  def submitDetails(userInformation: Input)(implicit hc: HeaderCarrier) : Future[String] = {
    val userJson = Json.toJson[Input](userInformation)
    val res = http.POST[JsValue, String]()
  }
}
*/
