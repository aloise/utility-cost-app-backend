package controllers.helpers

import javax.inject.Inject

import models.base.DBAccessProvider
import models._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.{Controller, Cookie, RequestHeader, Result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

/**
  * Created by aeon on 24/05/16.
  */


abstract class BaseController( ec:ExecutionContext, db:DBAccessProvider ) extends Controller with AuthAction with JsonResponses {

  val cookieMaxAge = 30 * 24 * 3600

  val authCookieName = "userId"
  val authTokenName = "Auth-Token"

  val userCookiePath = "/"


  def getAuthToken( user: models.User) =
    encryptObjId(user.id.getOrElse(-1))

  def getAuthCookie(user: models.User, rememberMe: Boolean = false) = {
    Cookie(authCookieName, getAuthToken(user), if (rememberMe) Some(cookieMaxAge) else None, userCookiePath, httpOnly = false)
  }

  implicit def getObject(request: RequestHeader): Future[models.User] = {

    val requestToken = request.cookies.get(authCookieName).map(_.value) orElse request.headers.get(authTokenName)

    val userIdOpt = requestToken.flatMap { token =>
      decryptObjId(token).toOption
    }

    db.run(UsersQuery.findById(userIdOpt.getOrElse(-1))).map{
      case Some(u) => u
      case None => throw new Exception("user_not_found")
    }
  }

  implicit def onUnauthorized(t: Throwable, request: RequestHeader): Result = {
    Unauthorized(Json.obj("error" -> true, "message" -> t.getMessage))
  }

  def getSecretToken( )(implicit conf:play.api.Configuration) = {
    val default = "%APPLICATION_SECRET%"

    val secret = conf.getString("application.secret").getOrElse(default)
    if( secret == default ){
      // Log the warning
    }

    secret

  }

}

