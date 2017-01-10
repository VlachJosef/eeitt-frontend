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

import org.scalatest.EitherValues
import play.api.mvc.PathBindable
import uk.gov.hmrc.eeitt.models.{ DryRun, Live }
import uk.gov.hmrc.play.test.UnitSpec

class ImportModeBinderSpec extends UnitSpec with EitherValues {

  "ImportModeBinder" should {
    "bind string to DryRun" in {
      val binder = ImportModeBinder.importModeBinder
      binder.bind("", "dry-run").right.value should be(DryRun)
    }

    "unbind DryRun" in {
      val binder = ImportModeBinder.importModeBinder
      binder.unbind("", DryRun) should be("dry-run")
    }

    "bind string to Live" in {
      val binder = ImportModeBinder.importModeBinder
      binder.bind("", "live").right.value should be(Live)
    }

    "unbind Live" in {
      val binder = ImportModeBinder.importModeBinder
      binder.unbind("", Live) should be("live")
    }

    "fail when trying to bind wrong string" in {
      val binder = ImportModeBinder.importModeBinder
      binder.bind("", "wrong-string").left.value should be("No valid import mode: wrong-string. Use 'dry-run' for testing or 'live' for real import.")
    }
  }
}
