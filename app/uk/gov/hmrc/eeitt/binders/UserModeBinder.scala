/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.eeitt.binders

import play.api.libs.json.{ JsError, JsString, JsSuccess, Json }
import play.api.mvc.PathBindable
import uk.gov.hmrc.eeitt.models.{ UserMode, Agents, BusinessUsers }

object UserModeBinder {
  implicit def userModeBinder(implicit stringBinder: PathBindable[String]) =
    new PathBindable[UserMode] {
      def bind(key: String, value: String): Either[String, UserMode] =
        stringBinder.bind(key, value).right.flatMap(parseUserModeString)

      override def unbind(key: String, userMode: UserMode): String =
        stringBinder.unbind(key, userMode.toString)
    }
  private def parseUserModeString(userModeString: String) = {
    userModeString match {
      case "agents" => Right(Agents)
      case "business-users" => Right(BusinessUsers)
      case _ => Left(s"No valid user mode: $userModeString. Use 'agents' or 'business-users'.")
    }
  }
}
