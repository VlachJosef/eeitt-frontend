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

import java.io.File
import java.util.Date

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, FlatSpec}

import play.api.libs.concurrent.Promise
import play.api.libs.iteratee.{Iteratee, Enumerator}

import scala.concurrent.ExecutionContext.Implicits.global


class TestSpec extends FlatSpec with Matchers with ScalaFutures {

  "blah" should "work" in {


      val enum = Enumerator.repeatM(
        Promise.timeout(
          "current time %s".format(new Date()), 5
        )
      )


      val iteratee: Iteratee[Int, Int] = Iteratee.fold(0)(_+_)
      val iteratee2: Iteratee[String, String] = Iteratee.fold("")(_+_)

      println(Enumerator[Int](1, 2, 3, 4).run(iteratee).futureValue)
      println(Enumerator[Int](5, 2, 3, 4).run(iteratee).futureValue)
      println(enum.run(iteratee2).futureValue)






//      Enumerator.fromFile(new File("myfile.txt"))
  }



}
