package intergation

/**
  * User: aloise
  * Date: 30.05.16
  * Time: 13:20
  */

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

class ServicesApiSpec extends PlaySpec with InitialSetup {

  "Service Api" must {



    var newServiceId:Int = 0
    var authToken:String = ""

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

    "have the service access" in {
      val response = await( wsClient.url( s"$apiGateway/services/1/access" ).withHeaders( authHeaders(authToken):_* ).get() )

      response.status mustBe OK

      val js = Json.parse(response.body)

      ( js \ "access" ).as[Boolean] mustBe true

    }

    "return a service by id" in {
      val response = await( wsClient.url( s"$apiGateway/services/1" ).withHeaders( authHeaders(authToken):_* ).get() )
      val js = Json.parse(response.body)

      response.status mustBe OK
      ( js \ "service" \ "id").as[Int] mustBe 1

    }

    "return a list of services by place" in {
      // should return 3 services
      val response = await( wsClient.url( s"$apiGateway/places/1/services" ).withHeaders( authHeaders(authToken):_* ).get() )
      val js = Json.parse(response.body)

      response.status mustBe OK
      ( js \ "services" \\ "id" ).map( _.as[Int] ) must contain allOf( 1,2,3 )

    }

    "create a new service" in {

      val serviceJson = Json.obj(
        "title" -> "New Test Title",
        "area" -> "Area",
        "description" -> "New Test Description",
        "createdByUserId" -> 0,
        "isDeleted" -> false
      )

      val response = await( wsClient.url( s"$apiGateway/services" ).withHeaders( authHeaders(authToken):_* ).post(serviceJson) )
      val js = Json.parse( response.body )
      val newServiceIdOpt = ( js \ "service" \ "id" ).asOpt[Int]

      response.status mustBe OK
      ( js \ "service" ).asOpt[JsObject] mustBe defined
      newServiceIdOpt mustBe defined

      // request the service
      newServiceId = newServiceIdOpt.get

      val getResponse = await( wsClient.url( s"$apiGateway/services/$newServiceId" ).withHeaders( authHeaders(authToken):_* ).get() )
      val getJs = Json.parse( getResponse.body )
      getResponse.status mustBe OK
      ( getJs \ "service" \ "id" ).as[Int] mustBe newServiceId
      ( serviceJson - "createdByUserId" ).fields.foreach { case (jsKey, jsValue) =>
        ( getJs \ "service" \ jsKey ).as[JsValue] mustBe jsValue
      }

    }

    "update existing service" in {
      val serviceJson = Json.obj(
        "id" -> 1,
        "title" -> "New Test Title 2",
        "area" -> "Area2",
        "description" -> "New Test Description 2",
        "createdByUserId" -> 0, // is not updated by service
        "isDeleted" -> false
      )

      val response = await( wsClient.url( s"$apiGateway/services" ).withHeaders( authHeaders(authToken):_* ).put(serviceJson) )
      val js = Json.parse( response.body )

      response.status mustBe OK
      ( js \ "service" ).as[JsObject] mustBe serviceJson

    }

    "attach a new service to the place" in {

      val responsePost = await( wsClient.url( s"$apiGateway/places/1/services/"+newServiceId ).withHeaders( authHeaders(authToken):_* ).post("") )

      responsePost.status mustBe OK

      val response = await( wsClient.url( s"$apiGateway/places/1/services" ).withHeaders( authHeaders(authToken):_* ).get() )
      val js = Json.parse(response.body)

      response.status mustBe OK
      ( js \ "services" \\ "id" ).map( _.as[Int] ).toSet mustBe Set( 1,2,3, newServiceId )

    }

    "delete a new service from the place" in {

      val responsePost = await( wsClient.url( s"$apiGateway/places/1/services/"+newServiceId ).withHeaders( authHeaders(authToken):_* ).delete() )

      responsePost.status mustBe OK

      val response = await( wsClient.url( s"$apiGateway/places/1/services" ).withHeaders( authHeaders(authToken):_* ).get() )
      val js = Json.parse(response.body)

      response.status mustBe OK
      ( js \ "services" \\ "id" ).map( _.as[Int] ).toSet mustBe Set( 1,2,3 )

    }

    "delete services" in {

      // attach it back
      val responseAddToPlace = await( wsClient.url( s"$apiGateway/places/1/services/"+newServiceId ).withHeaders( authHeaders(authToken):_* ).post("") )
      responseAddToPlace.status mustBe OK

      val responseDelete = await( wsClient.url( s"$apiGateway/services/"+newServiceId ).withHeaders( authHeaders(authToken):_* ).delete() )
      responseDelete.status mustBe OK

      val getResponse = await( wsClient.url( s"$apiGateway/services/$newServiceId" ).withHeaders( authHeaders(authToken):_* ).get() )
      getResponse.status must be !== OK

      val placeServices = await( wsClient.url( s"$apiGateway/places/1/services" ).withHeaders( authHeaders(authToken):_* ).get() )
      ( Json.parse( placeServices.body ) \ "services" \\ "id" ).map( _.as[Int] ).toSet mustBe Set( 1,2,3 )

    }


  }
}
