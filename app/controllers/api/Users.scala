package controllers.api

import javax.inject.Inject

import models.UsersQuery
import models.base.DBAccessProvider
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
class Users @Inject() ( implicit ec:ExecutionContext, conf:play.api.Configuration, db:DBAccessProvider ) extends ApiController(ec, db) {

  import models.helpers.JsonModels._

  val authDataReader = (
      (__ \ "email").read[String] and
      (__ \ "password").read[String]
  ).tupled

  val passwordSubst = ""


  def auth = Action.async(parse.json) { request =>
    request.body.validate(authDataReader).map {
      case (email, password) =>

        UsersQuery.authenticate(email, password, getSecretToken() ).map {
          case Some(user) =>
            jsonStatusOk( Json.obj( "user" -> user.copy(password = passwordSubst), "token" -> getAuthToken( user ) ) )
          case None =>
            Unauthorized(Json.obj("status" -> "error", "message" -> "unauthorized"))
        }
    } recoverTotal recoverJsonErrorsFuture
  }

  def info = withAuthAsync[models.User] { user => request =>
    jsonStatusOkFuture( Json.obj( "user" -> user.copy(password = passwordSubst) ) )
  }

}
