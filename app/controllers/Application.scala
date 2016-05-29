package controllers

import java.time.LocalDateTime
import javax.inject.Inject

import controllers.helpers.BaseController
import models.base.DBAccessProvider
import models._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.functional.syntax._
import play.api.libs.json.Json._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import models.helpers.ModelToJsonHelper._

import scala.concurrent.ExecutionContext
import play.api.Configuration
import slick.driver.H2Driver.api._


class Application @Inject()(implicit ec: ExecutionContext, conf: Configuration, db: DBAccessProvider) extends BaseController(ec, db) {

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
    Ok("Utility Billing Cost App")
  }

  def login = Action.async(parse.json) { implicit request =>
    request.body.validate(loginDataReader).map { case (email, password, rememberMe) =>

      UsersQuery.authenticate(email, password, getSecretToken()).map {
        case Some(user) => jsonStatusOk.withCookies(getAuthCookie(user, rememberMe.getOrElse(false)))
        case None => Unauthorized(Json.obj("status" -> "error", "message" -> "unauthorized"))
      }.recover { case t: Throwable => recoverJsonException(t) }
    }.recoverTotal(recoverJsonErrorsFuture)
  }

  def signup = Action.async(parse.json) { implicit request =>
    request.body.validate(signupDataReader).map { case (email, name, password) =>

      val u = User(
        name = name,
        email = email,
        password = password,
        created = LocalDateTime.now()
      )

      db.run(UsersQuery.filter(_.email === email).result.headOption).flatMap {
        case Some(_) => recoverJsonErrorsFuture("email_not_available")
        case None =>
          db.run(UsersQuery.signup(u, getSecretToken())).flatMap(id => db.run(UsersQuery.findById(id))).map {
            case Some(u) => jsonStatusOk(Json.obj("user" -> toJson(u))).withCookies(getAuthCookie(u))
            case None => recoverJsonErrors("db_error")
          }.recover { case t: Throwable => recoverJsonException(t) }
      }.recover { case t: Throwable => recoverJsonException(t) }
    } recoverTotal recoverJsonErrorsFuture
  }


}