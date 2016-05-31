package intergation

import controllers.helpers.AuthAction
import models.base.DBAccessProvider
import org.scalatestplus.play.{OneServerPerSuite, OneServerPerTest, PlaySpec}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.test.Helpers._

/**
  * User: aloise
  * Date: 30.05.16
  * Time: 15:28
  */

class UserApiSpec extends PlaySpec with InitialSetup {



  "User Api" must {


    var authToken:String = ""
    val userTestEmail = "test1@email.com"
    val userTestPassword = "pass1"

    "should not authorize the user with invalid password" in {
      val requestBody = Json.obj("email" ->  userTestEmail, "password" -> userTestPassword*2 )
      val response = await(wsClient.url(s"$apiGateway/users/auth").post(requestBody))
      val js = Json.parse(response.body)

      response.status must be !== OK
      ( js \ "status" ).as[String] mustBe "error"
      ( js \ "message" ).as[String] mustBe "unauthorized"

    }

    "authorize the user" in {
      val requestBody = Json.obj("email" -> userTestEmail, "password" -> userTestPassword )
      val response = await(wsClient.url(s"$apiGateway/users/auth").post(requestBody))
      val js = Json.parse(response.body)

      val newToken = (js \ "token").asOpt[String]

      response.status mustBe OK
      (js \ "status").as[String] mustBe "ok"
      newToken mustBe defined

      authToken = newToken.getOrElse("")

    }

    "return the user info" in {
      val response = await(wsClient.url(s"$apiGateway/users/info").withHeaders( authHeaders( authToken):_* ).get())
      val js = Json.parse(response.body)

      response.status mustBe OK
      ( js \ "status" ).as[String] mustBe "ok"
      ( js \ "user" \ "email" ).as[String] mustBe userTestEmail

    }

  }

}
