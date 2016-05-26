package controllers.helpers

import javax.inject.Inject

import models.Users
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.{Controller, Cookie, RequestHeader, Result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

/**
  * Created by aeon on 24/05/16.
  */


abstract class BaseController( ec:ExecutionContext, users:models.Users ) extends Controller with AuthAction with JsonResponses {

  val cookieMaxAge = 30 * 24 * 3600

  val authCookieName = "userId"
  val authTokenName = "Auth-Token"

  val userCookiePath = "/"



  def getAuthCookie(user: models.User, rememberMe: Boolean = false) = {
    Cookie(authCookieName, encryptObjId(user.id.getOrElse(-1)), if (rememberMe) Some(cookieMaxAge) else None, userCookiePath, httpOnly = false)
  }


  implicit def getObject(request: RequestHeader): Future[models.User] = {

    val requestToken = request.cookies.get(authCookieName).map(_.value) orElse request.headers.get(authTokenName)

    val userIdOpt = requestToken.flatMap { token =>
      decryptObjId(token).toOption
    }

    userIdOpt.map { userId =>
      users.findByIdOpt(userId).map {
        case Some(user) =>
          user
        case None =>
          throw new Exception("user_not_found")
      }(ec)
    }.getOrElse(Future.failed(new Exception("incorrect_cookie")))
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

