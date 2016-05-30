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

    "server should return a homepage" in {
      val response = await(wsClient.url(s"http://$address/").get())
      response.status mustBe OK
    }

    "authorize the user" in {
      val requestBody = Json.obj("email" -> "test1@email.com", "password" -> "pass1")
      val response = await(wsClient.url(s"http://$address/api/users/auth").post(requestBody))
      val js = Json.parse(response.body)

      val newToken = (js \ "token").asOpt[String]

      response.status mustBe OK
      (js \ "status").as[String] mustBe "ok"
      newToken mustBe defined

      authToken = newToken.getOrElse("")

    }

    "should not authorize the user with invalid password" in {
      val requestBody = Json.obj("email" -> "test1@email.com", "password" -> "OLOLOLOL")
      val response = await(wsClient.url(s"http://$address/api/users/auth").post(requestBody))

      response.status must be !== OK

    }
  }

}
