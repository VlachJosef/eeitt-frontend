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

package uk.gov.hmrc.eeitt

import play.api.Play.current
import play.api.libs.json._
import play.api.{Logger, LoggerLike, Play}
import play.twirl.api.Html
import uk.gov.hmrc.eeitt.WSHttp
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.ws.WSPost
import uk.gov.hmrc.play.partials.{HeaderCarrierForPartials, HtmlPartial}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


/* Created by harrison on 15/09/16.
*/

trait DfsConnector {
  /**
    * @return
    *
    */
  def http: WSPost

  /**
    * @return
    */
  def url: String

  /**
    * @return
    */

  def logger: LoggerLike

  /**
    * @return
    */
  def errorDisplay = uk.gov.hmrc.eeitt.views.html.formerror.render()

  /**
    * @param returnUrl
    * @param utr
    * @param hc
    * @return
    */
  def renderForm (returnUrl: String, utr: String)(implicit hc: HeaderCarrierForPartials): Future[Html] = {

    implicit val foo = hc.toHeaderCarrier

    http.POST[DfsRequestBody, HtmlPartial](url, DfsRequestBody.create(returnUrl, utr)).
      map {
        case HtmlPartial.Success(_, content) => content
        case HtmlPartial.Failure(Some(status), body) =>
          logger.warn(s"Failed to get the DFS form partial, response status: $status content: $body")
          errorDisplay
        case _ => errorDisplay
      }.
      recover {
        case ex => println(s"Hello: ${ex.getMessage}"); errorDisplay
      }
  }
}


object DfsConnector extends DfsConnector with ServicesConfig {
  override val http: WSPost = WSHttp
  override lazy val url: String = {
    val formTypeRef: String =
      Play.configuration.getString(s"$env.dfs-frontend.formtype").
        getOrElse(throw new Exception(s"env.dfs-frontend.formtype is missing"))
    s"${baseUrl("dfs-frontend")}/forms/form/$formTypeRef/new"
  }
  override val logger = Logger
}

case class DfsRequestBody(displayFormAsPartial: Boolean,
                          saveEnabled: Boolean,
                          afterSubmissionURI: String,
                          secureMailboxURL: String)

object DfsRequestBody {


  implicit val requestWrites: Writes[DfsRequestBody] = new Writes[DfsRequestBody] {
    /**
      * @param o
      * @return
      */
    override def writes(o: DfsRequestBody) = Json.obj(
      "displayFormAsPartial" -> o.displayFormAsPartial,
      "saveEnabled" -> o.saveEnabled,
      "afterSubmissionURI" -> o.afterSubmissionURI,
      "secureMailboxURL" -> o.secureMailboxURL
    )
  }

  /**
    * @param returnUrl
    * @param utr
    * @return
    */
  def create(returnUrl: String, utr: String): DfsRequestBody = DfsRequestBody(
    displayFormAsPartial = true,
    saveEnabled = true,
    afterSubmissionURI = returnUrl,
    secureMailboxURL = SecureMessageUrl.mailboxUrl(utr)
  )
}


trait SecureMessageUrl {
  /**
    * @return
    */
  def baseUrl: String

  /**
    * @param id
    * @return
    */
  def mailboxUrl(id: String) = s"$baseUrl/secure-message/inbox/$id"
}

object SecureMessageUrl extends SecureMessageUrl with ServicesConfig {
  lazy val baseUrl: String = baseUrl("contact-advisors-frontend")
}
