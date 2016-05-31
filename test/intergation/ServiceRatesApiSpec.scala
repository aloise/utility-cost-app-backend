package intergation

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


    var newServiceId: Int = 0
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

  }

}
