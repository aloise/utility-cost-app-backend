package controllers.helpers

import javax.inject.Inject

import models.Users
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.{Controller, Cookie, RequestHeader, Result}

import scala.concurrent.Future

/**
  * Created by aeon on 24/05/16.
  */


class BaseController @Inject()() extends Controller with AuthAction with JsonResponses {

  val cookieMaxAge = 30 * 24 * 3600

  val authCookieName = "userId"

  val userCookiePath = "/"

  val appBuilder = GuiceApplicationBuilder()

  val injector = appBuilder.injector()



  def getAuthCookie(user: models.User, rememberMe: Boolean = false) = {
    Cookie(authCookieName, encryptObjId(user.id), if (rememberMe) Some(cookieMaxAge) else None, userCookiePath, httpOnly = false)
  }


  implicit def getObject( request:RequestHeader ): Future[models.User] = {

    val userIdOpt = request.cookies.get(authCookieName).flatMap{ cookie =>
      decryptObjId(cookie.value).toOption
    }

    userIdOpt.map{ userId =>
      val users = injector.instanceOf[Users]
      users.findById(userId).map{
        case Some(user) =>
          user
        case None =>
          throw new Exception("user_not_found")
      }
    }.getOrElse( Future.failed(new Exception("incorrect_cookie")) )
  }

  implicit def onUnauthorized(t: Throwable, request: RequestHeader): Result = {
    Unauthorized(Json.obj("error" -> true, "message" -> t.getMessage))
  }

}

