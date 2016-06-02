package intergation

import java.time.LocalDateTime
import akka.stream.Materializer
import controllers.helpers.AuthAction
import models.base.DBAccessProvider
import org.scalatestplus.play._
import play.api.cache.EhCacheModule
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws._
import play.api.mvc._
import play.api.test._
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.libs.functional.syntax._
/**
  * User: aloise
  * Date: 30.05.16
  * Time: 22:35
  */
class ServiceRatesApiSpec extends PlaySpec with InitialSetup {

  "Rate Api" must {


    var newServiceRateId: Int = 0
    var authToken: String = ""

    "authorize the user" in {
      val requestBody = Json.obj("email" -> "test1@email.com", "password" -> "pass1")
      val response = await(wsClient.url(s"$apiGateway/users/auth").post(requestBody))
      val js = Json.parse(response.body)

      val newToken = (js \ "token").asOpt[String]

      response.status mustBe OK
      (js \ "status").as[String] mustBe "ok"
      newToken mustBe defined

      authToken = newToken.getOrElse("")

    }

    "return a rate by id" in {
      val response = await( wsClient.url( s"$apiGateway/rates/1" ).withHeaders( authHeaders(authToken):_* ).get() )
      val js = Json.parse(response.body)

      response.status mustBe OK
      ( js \ "serviceRate" \ "id").as[Int] mustBe 1

    }

    "return a list of active rates for service" in {
      val response = await( wsClient.url( s"$apiGateway/services/1/rates" ).withHeaders( authHeaders(authToken):_* ).get() )
      val js = Json.parse(response.body)

      response.status mustBe OK
      ( js \ "serviceRates" \\ "id").map(_.as[Int]) must contain ( 1 )

    }

    "return a list of all rates for service" in {
      val response = await( wsClient.url( s"$apiGateway/services/1/rates" ).withQueryString( "includeInactive" -> "1" ).withHeaders( authHeaders(authToken):_* ).get() )
      val js = Json.parse(response.body)

      response.status mustBe OK
      ( js \ "serviceRates" \\ "id").map(_.as[Int]) must contain allOf( 1,2 )

    }

    "create a new service rate" in {
      val serviceRateJson = Json.obj(
        "serviceId" -> 1,
        "isActive" -> true,
        "activeFromDate" -> LocalDateTime.now(),
        "rateData" -> Json.obj("ManualPriceRateData" -> Json.obj()),
        "isDeleted" -> false
      )

      val response = await( wsClient.url( s"$apiGateway/rates" ).withHeaders( authHeaders(authToken):_* ).post(serviceRateJson) )
      val js = Json.parse( response.body )
      val newServiceIdOpt = ( js \ "serviceRate" \ "id" ).asOpt[Int]

      response.status mustBe OK
      ( js \ "serviceRate" ).asOpt[JsObject] mustBe defined
      newServiceIdOpt mustBe defined

      // request the service
      newServiceRateId = newServiceIdOpt.get

      val getResponse = await( wsClient.url( s"$apiGateway/rates/$newServiceRateId" ).withHeaders( authHeaders(authToken):_* ).get() )
      val getJs = Json.parse( getResponse.body )
      getResponse.status mustBe OK
      ( getJs \ "serviceRate" \ "id" ).as[Int] mustBe newServiceRateId
      serviceRateJson.fields.foreach { case (jsKey, jsValue) =>
        ( getJs \ "serviceRate" \ jsKey ).as[JsValue] mustBe jsValue
      }

    }

    "update existing service rate" in {
      val serviceJson = Json.obj(
        "id" -> 1,
        "serviceId" -> 1,
        "isActive" -> true,
        "activeFromDate" -> LocalDateTime.now(),
        "rateData" -> Json.obj("ManualPriceRateData" -> Json.obj()),
        "isDeleted" -> false
      )

      val response = await( wsClient.url( s"$apiGateway/rates" ).withHeaders( authHeaders(authToken):_* ).put(serviceJson) )
      val js = Json.parse( response.body )

      response.status mustBe OK
      ( js \ "serviceRate" ).as[JsObject] mustBe serviceJson

    }

    "return a list of all rates for service with new one" in {
      val response = await( wsClient.url( s"$apiGateway/services/1/rates" ).withQueryString( "includeInactive" -> "1" ).withHeaders( authHeaders(authToken):_* ).get() )
      val js = Json.parse(response.body)

      response.status mustBe OK
      ( js \ "serviceRates" \\ "id").map(_.as[Int]).toSet mustBe Set( 1,2, newServiceRateId )

    }

    "set service rate active status to 0" in {

      val responsePut = await( wsClient.url( s"$apiGateway/rates/1/active/0" ).withHeaders( authHeaders(authToken):_* ).put("") )
      responsePut.status mustBe OK

      val response = await( wsClient.url( s"$apiGateway/rates/1" ).withHeaders( authHeaders(authToken):_* ).get() )
      val js = Json.parse(response.body)

      response.status mustBe OK
      ( js \ "serviceRate" \ "id").as[Int] mustBe 1
      ( js \ "serviceRate" \ "isActive").as[Boolean] mustBe false

    }

    "set service rate active status to 1" in {

      val responsePut = await( wsClient.url( s"$apiGateway/rates/1/active/1" ).withHeaders( authHeaders(authToken):_* ).put("") )
      responsePut.status mustBe OK


      val response = await( wsClient.url( s"$apiGateway/rates/1" ).withHeaders( authHeaders(authToken):_* ).get() )
      val js = Json.parse(response.body)

      response.status mustBe OK
      ( js \ "serviceRate" \ "id").as[Int] mustBe 1
      ( js \ "serviceRate" \ "isActive").as[Boolean] mustBe true

    }

    "delete services rate" in {

      val responsePost = await( wsClient.url( s"$apiGateway/rates/" + newServiceRateId ).withHeaders( authHeaders(authToken):_* ).delete() )

      responsePost.status mustBe OK

      // attach it back
      val response = await( wsClient.url( s"$apiGateway/services/1/rates" ).withQueryString( "includeInactive" -> "1" ).withHeaders( authHeaders(authToken):_* ).get() )
      val js = Json.parse(response.body)

      response.status mustBe OK
      ( js \ "serviceRates" \\ "id").map(_.as[Int]).toSet mustBe Set( 1,2 )
    }


  }

}
