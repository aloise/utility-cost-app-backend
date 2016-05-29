package controllers.api

import javax.inject.Inject

import controllers.helpers.BaseController
import models.base.DBAccessProvider
import play.api.libs.json._
import play.api.mvc.{Request, RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}

/**
  * User: aloise
  * Date: 26.05.16
  * Time: 12:58
  */
abstract class ApiController ( ec:ExecutionContext, db:DBAccessProvider ) extends BaseController( ec, db ) {

  def apiWithAuth(bodyFunc: => models.User => Request[JsValue] => Future[Result]) =
    withAuthJsonAsync[models.User]{ user => request =>
      bodyFunc( user )( request ).recover{
        case ex:Throwable => recoverJsonException(ex)
      }(ec)
    }( getObject, onUnauthorized )

  def apiWithParser[X]( parser: Reads[X] )(f: => models.User => X => Future[Result]) =
    withAuthJsonAsync[models.User]{ user => requestWithJsValue =>
      requestWithJsValue.body.validate[X]( parser ).fold(
        { errors =>
          recoverJsonErrorsFuture( new JsError( errors ) )
        },
        { parseResult =>
          f(user)( parseResult )
        }
      )

    } ( getObject, onUnauthorized )




}
