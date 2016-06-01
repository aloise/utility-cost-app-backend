package intergation

import java.time.LocalDateTime

import akka.stream.Materializer
import controllers.helpers.AuthAction
import models.base.DBAccessProvider
import org.joda.money.{CurrencyUnit, Money}
import org.scalatestplus.play._
import play.api.cache.EhCacheModule
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws._
import play.api.mvc._
import play.api.test._
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.libs.functional.syntax._
import models.helpers.JsonJodaMoney._

/**
  * User: aloise
  * Date: 01.06.16
  * Time: 18:57
  */
class BillsApiSpec extends PlaySpec with InitialSetup {

  "Rate Api" must {

    var newBillId: Int = 0
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

    "return a bill by id" in {
      val response = await( wsClient.url( s"$apiGateway/bills/1" ).withHeaders( authHeaders(authToken):_* ).get() )
      val js = Json.parse(response.body)

      response.status mustBe OK
      ( js \ "bill" \ "id").as[Int] mustBe 1

    }

    "return a list of bills by service" in {
      val response = await( wsClient.url( s"$apiGateway/services/1/bills" ).withHeaders( authHeaders(authToken):_* ).get() )
      val js = Json.parse(response.body)

      response.status mustBe OK
      ( js \ "bills" \\ "id").map( _.as[Int]).toSet mustBe Set( 1 )
    }

    "return a list of bills by service rate" in {
      val response = await( wsClient.url( s"$apiGateway/rates/1/bills" ).withHeaders( authHeaders(authToken):_* ).get() )
      val js = Json.parse(response.body)

      response.status mustBe OK
      ( js \ "bills" \\ "id").map( _.as[Int]).toSet mustBe Set( 1 )
    }

    "return a list of bills by place" in {
      val response = await( wsClient.url( s"$apiGateway/places/1/bills" ).withHeaders( authHeaders(authToken):_* ).get() )
      val js = Json.parse(response.body)

      response.status mustBe OK
      ( js \ "bills" \\ "id").map( _.as[Int]).toSet mustBe Set( 1,2 ,3 )
    }

    "create a new bill" in {

      val billJson = Json.obj(
        "placeId" -> 1,
        "serviceId" -> 1,
        "serviceRateId" -> 1,
        "readout" -> BigDecimal(115),// service - readout
        "value" -> Money.of( CurrencyUnit.EUR, 15 ), // how much does it cost
        "created" -> LocalDateTime.now(),
        "paid" -> Some( LocalDateTime.now() ),
        "isDeleted" -> false
      )

      val response = await( wsClient.url( s"$apiGateway/bills" ).withHeaders( authHeaders(authToken):_* ).post(billJson) )
      val js = Json.parse( response.body )
      val newBillIdOpt = ( js \ "bill" \ "id" ).asOpt[Int]

      response.status mustBe OK
      ( js \ "bill" ).asOpt[JsObject] mustBe defined
      newBillIdOpt mustBe defined

      // request the service
      newBillId = newBillIdOpt.get

      val getResponse = await( wsClient.url( s"$apiGateway/bills/$newBillId" ).withHeaders( authHeaders(authToken):_* ).get() )
      val getJs = Json.parse( getResponse.body )
      getResponse.status mustBe OK
      ( getJs \ "bill" \ "id" ).as[Int] mustBe newBillId
      billJson.fields.foreach { case (jsKey, jsValue) =>
        ( getJs \ "bill" \ jsKey ).as[JsValue] mustBe jsValue
      }

      val response2 = await( wsClient.url( s"$apiGateway/services/1/bills" ).withHeaders( authHeaders(authToken):_* ).get() )
      val js2 = Json.parse(response2.body)

      response2.status mustBe OK
      ( js2 \ "bills" \\ "id").map( _.as[Int]).toSet mustBe Set( 1, newBillId )

    }

    "update an existing bill" in {
      val billJson = Json.obj(
        "id" -> 1,
        "placeId" -> 1,
        "serviceId" -> 1,
        "serviceRateId" -> 1,
        "readout" -> BigDecimal(125),// service - readout
        "value" -> Money.of( CurrencyUnit.EUR, 15 ), // how much does it cost
        "created" -> LocalDateTime.now(),
        "paid" -> Some( LocalDateTime.now() ),
        "isDeleted" -> false
      )

      val response = await( wsClient.url( s"$apiGateway/bills" ).withHeaders( authHeaders(authToken):_* ).put(billJson) )
      val js = Json.parse( response.body )

      response.status mustBe OK
      ( js \ "bill" ).as[JsObject] mustBe billJson



    }

    "delete bill" in {

      val responsePost = await( wsClient.url( s"$apiGateway/bills/" + newBillId ).withHeaders( authHeaders(authToken):_* ).delete() )
      responsePost.status mustBe OK

      val response = await( wsClient.url( s"$apiGateway/services/1/bills" ).withHeaders( authHeaders(authToken):_* ).get() )
      val js = Json.parse(response.body)

      response.status mustBe OK
      ( js \ "bills" \\ "id").map(_.as[Int]).toSet mustBe Set( 1 )


    }

  }

}
