import junit.framework.TestCase
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{eq => sameAs}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import play.api.http.Status
import play.api.{LoggerLike, Play}
import play.twirl.api.Html
import uk.gov.hmrc.eeitt.DfsConnector
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.ws.WSPost
import uk.gov.hmrc.play.partials.HeaderCarrierForPartials
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

/**
  * Created by harrison on 03/10/16.
  */
class ConnectorSpec extends UnitSpec
  with WithFakeApplication
  with ScalaFutures
  with DfsFrontEndServer
  with IntegrationPatience
  with MockitoSugar {
  "DFS connector" should {
    "POST a properly formatted payload" in new TestCase {
      givenDfsRespondsWith(
        requestBody = expectedRequestBody,
        respoonseBody = expectedResponseBody
      )

      implicit val hc = HeaderCarrierForPartials(HeaderCarrier(), "")
      val response: Html = connector.renderForm(
        returnUrl = returnUrl,
        utr = utr
      ).futureValue

      repsonse shouldBe Html(expectedResponseBody)
    }

    "Display error message in HTML when call to DFS fails" in new TestCase {
      givenDfsRespondsWith(
        requestBody = expectedRequestBody,
        responseBody = expectedResponseBody,
        status = Status.INTERNAL_SERVER_ERROR
      )

      implicit val hc = HeaderCarrierForPartials(HeaderCarrier(), "")
      val response = connector.renderForm(
        returnUrl = returnUrl,
        utr = utr
      ).futureValue.toString

      response should include("id=\"errorMessage\"")
      response should include("Something went wrong. Please try again.")
    }

    "Display error message in HTML and log message when call to DFS returns BAD_REQUEST" in new TestCase {
      errorMessage = "It's all gone wrong"
      errorResponseBody =
        """{
          | "Status":400
          | "message": "$errorMessage"
          |}""".stripMargin

      givenDfsRespondsWith(
        requestBody = expectedRequestBody,
        responseBody = errorResponseBody,
        status = Status.BAD_REQUEST
      )

      val captor = ArgumentCaptor.forClass(classOf[() => String])

      implicit val hc = HeaderCarrierForPartials(HeaderCarrier(), "")
      val response = connector.renderForm(
        returnUrl = returnUrl,
        utr = utr
      ).futureValue.toString

      response should include("id=\"errorMessage\"")
      response should include("Something went wrong. Please try again.")

      verify(connector.logger).warn(captor.capture.apply)
      captor.getValue.apply should include(""""status": 400""")
      captor.getValue.apply should include(""""message": "It's all gone wrong"""")
    }
  }


  trait TestCase {
    val forType = "penalty-enquiry"
    val connector = new DfsConnector {

      override val logger = mock[LoggerLike]

      override def http = DfsConnector.http

      override def url = DfsConnector.url
    }

    val returnUrl = "/personal-account/messages/[encrypted-stuff]"
    val utr = "utr-123"


    val expectedRequestBody =
      s"""
         |{
         |   "displayFormAsPartial": true,
         |   "saveEnabled": false,
         |   "afterSubmissionURI": "$returnUrl",
         |   "secureMailboxURL": "http://localhost:9848/secure-message/inbox/$utr"
         | }
       """.stripMargin

     val expectedResponseBody = "<html></html>"
  }

}
