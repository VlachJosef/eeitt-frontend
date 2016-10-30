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

import play.api.mvc.Results._
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.eeitt.FrontendAppConfig
import uk.gov.hmrc.play.frontend.auth._

import scala.concurrent.Future

trait EeittAuth { self: UserActions =>
  def AuthenticatedAction(r: AuthContext => Request[AnyContent] => Result) =
    AuthenticatedBy(EeittAuthenticationProvider, EeittPageVisibilityPredicate)(r)

  def AsyncAuthenticatedAction(r: AuthContext => Request[AnyContent] => Future[Result]) =
    AuthenticatedBy(EeittAuthenticationProvider, EeittPageVisibilityPredicate).async(r)
}

object EeittAuthenticationProvider extends GovernmentGateway {
  override def redirectToLogin(implicit request: Request[_]): Future[Result] = {
    val queryStringParams = Map("continue" -> Seq(FrontendAppConfig.eeittFrontendBaseUrl + request.uri))
    Future.successful(Redirect(loginURL, queryStringParams))
  }

  def continueURL: String = "not used since we override redirectToLogin"

  def loginURL: String = FrontendAppConfig.governmentGatewaySignInUrl
}

object EeittPageVisibilityPredicate extends PageVisibilityPredicate {
  def apply(authContext: AuthContext, request: Request[AnyContent]): Future[PageVisibilityResult] =
    Future.successful(PageIsVisible)
}

