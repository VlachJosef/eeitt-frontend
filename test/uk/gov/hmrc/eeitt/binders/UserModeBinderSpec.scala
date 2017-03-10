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
import uk.gov.hmrc.eeitt.models.{ Agents, BusinessUsers }
import uk.gov.hmrc.play.test.UnitSpec

class UserModeBinderSpec extends UnitSpec with EitherValues {
  "UserModeBinder" should {
    "bind string to Agents" in {
      val binder = UserModeBinder.userModeBinder
      binder.bind("", "agents").right.value should be(Agents)
    }

    "unbind Agents" in {
      val binder = UserModeBinder.userModeBinder
      binder.unbind("", Agents) should be("agents")
    }

    "bind string to BusinessUsers" in {
      val binder = UserModeBinder.userModeBinder
      binder.bind("", "business-users").right.value should be(BusinessUsers)
    }

    "unbind BusinessUsers" in {
      val binder = UserModeBinder.userModeBinder
      binder.unbind("", BusinessUsers) should be("business-users")
    }

    "fail when trying to bind wrong string" in {
      val binder = UserModeBinder.userModeBinder
      binder.bind("", "wrong-string").left.value should be("No valid user mode: wrong-string. Use 'agents' or 'business-users'.")
    }
  }
}