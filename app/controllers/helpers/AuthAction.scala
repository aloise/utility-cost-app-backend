package controllers.helpers

import helpers.CryptoHelper
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.JsValue
import play.api.mvc._

import scala.concurrent.Future
import scala.util.Try


/**
  * User: aeon
  * Date: 12/03/16
  * Time: 00:06
  */

trait AuthAction {

  def withAuth[A, T](bodyParser: BodyParser[A])(f: => T => Request[A] => Result)(implicit getObject: RequestHeader => Future[T], onUnauthorized: (Throwable, RequestHeader) => Result): EssentialAction =
    Action.async(bodyParser) { request =>
      getObject(request) map { obj =>
        f(obj)(request)
      } recover {
        case t: Throwable =>
          onUnauthorized(t, request)
      }
    }


  def withAuth[T](f: => T => Request[AnyContent] => Result)(implicit getObject: RequestHeader => Future[T], onUnauthorized: (Throwable, RequestHeader) => Result): EssentialAction =
    withAuth[AnyContent, T](BodyParsers.parse.anyContent)(f)(getObject, onUnauthorized)

  def withAuthJson[T](f: => T => Request[JsValue] => Result)(implicit getObject: RequestHeader => Future[T], onUnauthorized: (Throwable, RequestHeader) => Result): EssentialAction =
    withAuth[JsValue, T](BodyParsers.parse.json)(f)(getObject, onUnauthorized)

  def withAuthAsync[A, T](bodyParser: BodyParser[A])(f: => T => Request[A] => Future[Result])(implicit getObject: RequestHeader => Future[T], onUnauthorized: (Throwable, RequestHeader) => Result): EssentialAction =
    Action.async(bodyParser) { request =>
      getObject(request) flatMap { obj =>
        f(obj)(request)
      } recover {
        case t: Throwable =>
          onUnauthorized(t, request)
      }
    }

  def withAuthAsync[T](f: => T => Request[AnyContent] => Future[Result])(implicit getObject: RequestHeader => Future[T], onUnauthorized: (Throwable, RequestHeader) => Result): EssentialAction =
    withAuthAsync[AnyContent, T](BodyParsers.parse.anyContent)(f)(getObject, onUnauthorized)

  def withAuthJsonAsync[T](f: => T => Request[JsValue] => Future[Result])(implicit getObject: RequestHeader => Future[T], onUnauthorized: (Throwable, RequestHeader) => Result): EssentialAction =
    withAuthAsync[JsValue, T](BodyParsers.parse.json)(f)(getObject, onUnauthorized)

  def encryptObjId( id : Int):String =
    CryptoHelper.encryptAES(id.toString)

  def decryptObjId( str:String ):Try[Int] = {
    Try{
      CryptoHelper.decryptAES(str).toInt
    }
  }

}