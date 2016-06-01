package intergation



import models.Place
import models.base.DBAccessProvider
import org.scalatestplus.play._
import play.api.Play
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.test.Helpers._
import play.api.test._
import org.scalatest.Matchers._


/**
  * User: aeon
  * Date: 30/05/16
  * Time: 15:10
  */
class PlacesIntegrationTestSpec extends PlaySpec with InitialSetup{

  val userTestEmail = users.head.email
  val userTestPassword = "pass1"


  "Places API" must {

    "return list of 3 items for first user" in {
      val authToken = auth(userTestEmail, userTestPassword)
      val response = await(wsClient.url(s"$apiGateway/places").withHeaders( "Auth-Token" -> authToken ).get())

      response.status mustBe OK
      val js = Json.parse(response.body)
      ( js \ "places" ).as[Array[JsValue]] should have length 3
    }

    "create place should return new id" in {

      val authToken = auth(userTestEmail, userTestPassword)

      val requestBody = Json.obj(
        "title" -> "Place_title",
        "country" -> "US",
        "city" -> "Fargo",
        "state" -> "ND",
        "zip" -> "58103",
        "address" -> "Street 1",
        "isDeleted" -> false
      )

      val response = await(wsClient.url(s"$apiGateway/places").withHeaders( "Auth-Token" -> authToken ).post(requestBody))
      response.status mustBe OK
      val js = Json.parse(response.body)

      ( js \ "place" \ "id" ).asOpt[Int] shouldBe defined

      ( js \ "place" \ "id" ).asOpt[Int].value shouldBe > (0)
    }

    "update place should return updated place record" in {

      val authToken = auth(userTestEmail, userTestPassword)

      val requestBody = Json.obj(
        "id" -> 1,
        "title" -> "Secret Place",
        "country" -> "US",
        "city" -> "Fargo",
        "state" -> "ND",
        "zip" -> "58103",
        "address" -> "Street 1",
        "isDeleted" -> false
      )

      val response = await(wsClient.url(s"$apiGateway/places").withHeaders( "Auth-Token" -> authToken ).put(requestBody))

      response.status mustBe OK

      val js = Json.parse(response.body)

      ( js \ "place" ).asOpt[JsValue] shouldBe defined

      ( js \ "place" ).asOpt[JsValue].value shouldBe requestBody

    }

    "delete place should return OK and be no longer available" in {
      val authToken = auth(userTestEmail, userTestPassword)
      val deleteResponse = await(wsClient.url(s"$apiGateway/places/2").withHeaders( "Auth-Token" -> authToken ).delete())
      deleteResponse.status mustBe OK

      val response = await(wsClient.url(s"$apiGateway/places/2").withHeaders( "Auth-Token" -> authToken ).get())

      response.status mustBe BAD_REQUEST

      val js = Json.parse(response.body)
      ( js \ "message" ).as[String] shouldBe "access_denied"
    }

  }


}
