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
import uk.gov.hmrc.eeitt.views.html.helloworld.hello_world
import play.api.mvc.{Action, Controller}
import uk.gov.hmrc.eeitt.Forms.CaptureForm

import scala.concurrent.Future

/**
  * Created by harrison on 14/09/16.
  */
object InputController extends Controller {
  def helloWorld = Action{ implicit request =>
    Ok(hello_world(CaptureForm.userInput))

  }


  def myRedirect = Action { implicit request =>
    val userData = CaptureForm.userInput.bindFromRequest.get

    print(userData.typeOf)
//
   Ok("http://localhost:9000/main/registration/details?" + userData.typeOf + userData.credential)


  }
}
