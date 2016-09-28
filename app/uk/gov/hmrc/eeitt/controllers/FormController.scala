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
import play.api.Play.current
import play.api.libs.ws
import play.api.libs.ws.{WS, WSResponse}
import play.api.mvc.{Action, Controller}
import uk.gov.hmrc.eeitt.views.html.dfsform
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
/**
  * Created by harrison on 27/09/16.
  */
object FormController extends Controller {

  def wsAction = Action.async { implicit request =>


     WS.url("http://localhost:9000/forms/form/claim-a-tax-refund/new").get().map { response =>
      Ok(dfsform(response.body))
    }
  }
}