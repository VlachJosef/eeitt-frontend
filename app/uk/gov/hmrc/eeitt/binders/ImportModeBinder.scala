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
import uk.gov.hmrc.eeitt.models.{ ImportMode, DryRun, Live }

object ImportModeBinder {
  implicit def importModeBinder(implicit stringBinder: PathBindable[String]) =
    new PathBindable[ImportMode] {
      def bind(key: String, value: String): Either[String, ImportMode] =
        stringBinder.bind(key, value).right.flatMap(parseImportModeString)

      override def unbind(key: String, importMode: ImportMode): String =
        stringBinder.unbind(key, importMode.toString)
    }
  private def parseImportModeString(importModeString: String) = {
    importModeString match {
      case "live" => Right(Live)
      case "dry-run" => Right(DryRun)
      case _ => Left(s"No valid import mode: $importModeString. Use 'dry-run' for testing or 'live' for real import.")
    }
  }
}
