package controllers

import java.time.LocalDateTime
import javax.inject.Inject

import controllers.helpers.BaseController
import models.{User, Users}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.functional.syntax._
import play.api.libs.json.Json._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

import models.helpers.ModelToJsonHelper._



class Application @Inject()(dbConfigProvider: DatabaseConfigProvider) extends BaseController {

  val loginDataReader = (
    (__ \ "email").read[String] and
      (__ \ "password").read[String] and
      (__ \ "rememberMe").readNullable[Boolean]
    ).tupled

  val signupDataReader = (
    (__ \ "email").read[String](email) and
      (__ \ "name").read[String](minLength[String](1)) and
      (__ \ "password").read[String](minLength[String](6))
    ).tupled


  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def login = Action.async(parse.json) { implicit request =>
    request.body.validate(loginDataReader).map { case (email, password, rememberMe) =>
      val users = injector.instanceOf[Users]
      users.authenticate(email, password).map {
        case Some(user) => jsonStatusOk.withCookies(getAuthCookie(user, rememberMe.getOrElse(false)))
        case None => Unauthorized(Json.obj("status" -> "error", "message" -> "unauthorized"))
      }.recover { case t: Throwable => recoverJsonException(t) }
    }.recoverTotal(recoverJsonErrorsFuture)
  }

  def signup = Action.async(parse.json) { implicit request =>
    request.body.validate(signupDataReader).map { case (email, name, password) =>
      val users = injector.instanceOf[Users]
      val u = User(
        name = name,
        email = email,
        password = password,
        created = LocalDateTime.now()
      )
      users.signup(u).map(u =>
        jsonStatusOk(Json.obj("user"->toJson(u))).withCookies(getAuthCookie(u))
      ).recover{case t:Throwable => recoverJsonException(t)}
    }recoverTotal recoverJsonErrorsFuture
  }


}