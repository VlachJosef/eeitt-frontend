package uk.gov.hmrc.eeitt.utils.utils

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

/**
  * Created by harrison on 03/10/16.
  */

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.Suite
import play.api.http.Status



trait DfsFrontEndServer extends WithWiremock{ suite: Suite =>

  override val dependenciesPort: Int = 9094

  val postUrl = "/forms/form/landfill-tax-group-member-details/new"

  def givenDfsRespondsSuccessfullyWith(responseBody: String) =
    wireMock.register(
      post(urlEqualTo(postUrl))
        .willReturn(aResponse()
          .withStatus(Status.OK)
          .withBody(responseBody
          )))

  def givenDfsRespondsWith(requestBody: String,
                           responseBody: String,
                           status: Int = Status.OK) =
    wireMock.register(
      post(urlEqualTo(postUrl))
        .withRequestBody(equalToJson(requestBody))
        .willReturn(aResponse()
          .withStatus(status)
          .withBody(responseBody)
        ))


}
