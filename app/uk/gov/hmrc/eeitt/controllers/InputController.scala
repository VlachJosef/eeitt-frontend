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

import play.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.WS
import play.api.mvc.{Action, Controller}
import uk.gov.hmrc.eeitt.Forms.CaptureForm
import uk.gov.hmrc.eeitt.Models._
import uk.gov.hmrc.eeitt.views.html.displayresponse
import uk.gov.hmrc.eeitt.views.html.helloworld.hello_world
import uk.gov.hmrc.eeitt.views.html.startpage
import uk.gov.hmrc.play.http.{HttpGet, HttpPost}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * Created by harrison on 14/09/16.
  */
trait ConnectorWIthHttpValues {
  val http: HttpGet with HttpPost {

  }
}
object InputController extends Controller {
  val helloWorld = Action.async { implicit request =>
    Future.successful(Ok(hello_world(CaptureForm.userInput)))
  }


  def asyncAction = Action.async { implicit request =>
    CaptureForm.userInput.bindFromRequest.fold(
      formWithErrors => Future.successful(Ok(hello_world(formWithErrors))),
      input => {
        val futureJsUserArray = WS.url("http://localhost:9092/main/registration/details?" + input.typeOf + input.credential).get()

        futureJsUserArray.map { jsResponse =>
          val successResponse = Json.parse(jsResponse.body).validate[SuccessResponse]
          successResponse match {
            case JsSuccess(response, _) => Ok(displayresponse(response))
            case e: JsError => InternalServerError("problem with a response fromm downstream service, details: " + e.errors)
          }

        }
      }
    )
  }


}




