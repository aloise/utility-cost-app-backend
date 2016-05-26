package controllers.api

import javax.inject.Inject

import play.api.libs.json._
import play.api.mvc.BodyParsers.parse
import play.api.libs.functional.syntax._
import play.api.libs.json.Json._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent.ExecutionContext

/**
  * User: aloise
  * Date: 26.05.16
  * Time: 12:37
  */
class Users @Inject() ( implicit ec:ExecutionContext, users:models.Users, conf:play.api.Configuration ) extends ApiController(ec, users) {

  val authDataReader = (
      (__ \ "email").read[String] and
      (__ \ "password").read[String]
  ).tupled



  def auth = apiWithParser(authDataReader) { user => {
    case (email, password) =>
      jsonStatusOkFuture()
    }
  }

}
