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
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

/**
  * Created by harrison on 21/09/16.
  */
object TestPageController extends Controller {
  val testPage = Action.async { implicit request =>
    Future.successful(Ok(uk.gov.hmrc.eeitt.views.html.testpage()))
  }

  val aPage = Action.async { implicit request =>
    Future.successful(Ok(uk.gov.hmrc.eeitt.views.html.justapage()))
  }


}