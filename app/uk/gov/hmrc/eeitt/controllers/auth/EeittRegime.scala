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

package uk.gov.hmrc.eeitt.controllers.auth

import uk.gov.hmrc.play.frontend.auth.connectors.domain.Accounts
import uk.gov.hmrc.play.frontend.auth.{GovernmentGateway, AuthenticationProvider, TaxRegime}

object EeittRegime extends TaxRegime {

  override def isAuthorised(accounts: Accounts): Boolean = {
    accounts

    true
  }

  override def authenticationType: AuthenticationProvider = new GovernmentGateway {
    override val login = "http://localhost:9025/gg/sign-in?continue=http://localhost:9000/eeitt-frontend/landingPage"
  }

  override def unauthorisedLandingPage: Option[String] = {
    println("********" + uk.gov.hmrc.eeitt.controllers.routes.ApplicationController.unauthorised().url)
    Some(uk.gov.hmrc.eeitt.controllers.routes.ApplicationController.unauthorised().url)
  }

}
