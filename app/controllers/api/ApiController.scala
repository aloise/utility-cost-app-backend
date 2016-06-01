package controllers.api

import javax.inject.Inject

import controllers.helpers.BaseController
import models.BillsQuery
import models.base.{DBAccessProvider, IndexedRow, ObjectAccess, UserHasAccess}
import play.api.libs.json._
import play.api.mvc.{Request, RequestHeader, Result}
import slick.lifted.Rep
import slick.driver.H2Driver.api._

import scala.concurrent.{ExecutionContext, Future}

/**
  * User: aloise
  * Date: 26.05.16
  * Time: 12:58
  */
abstract class ApiController ( ec:ExecutionContext, db:DBAccessProvider ) extends BaseController( ec, db ) {

  def apiWithAuth(bodyFunc: => models.User => Request[_] => Future[Result]) =
    withAuthAsync[models.User]{ user => request =>
      bodyFunc( user )( request ).recover{
        case ex:Throwable => recoverJsonException(ex)
      }(ec)
    }( getObject, onUnauthorized )


  /**
    *
    * @param hasAccess ( User.id, ObjectAccess )
    * @param bodyFunc Process the request
    * @return
    */
  def apiWithAuth( hasAccess: Rep[Int] => Rep[Boolean] )(bodyFunc: => models.User => Request[_] => Future[Result]) =
    withAuthAsync[models.User]{ user => request =>

      db.run( hasAccess( LiteralColumn( user.id.getOrElse(0) ) ).result ).flatMap {
        case true =>
          bodyFunc(user)(request).recover {
            case ex: Throwable => recoverJsonException(ex)
          }(ec)
        case _ =>
          jsonErrorAccessDenied
      }(ec)

    }( getObject, onUnauthorized )


  def apiWithAuthJson(bodyFunc: => models.User => Request[JsValue] => Future[Result]) =
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


  /**
    *
    * @param parser
    * @param hasAccess User.id -> Boolean
    * @param f
    */

  def apiWithParserHasAccess[X <: IndexedRow]( parser: Reads[X] )( hasAccess: Rep[Int] => Rep[Boolean] )(f: => models.User => X => Future[Result]) = {
    apiWithParser(parser) { user: models.User => parseResult: X =>
      db.run(hasAccess( LiteralColumn( user.id.getOrElse(0) ) ).result).flatMap {
        case true =>
          f(user)(parseResult)
        case _ =>
          jsonErrorAccessDenied
      }(ec)
    }
  }



  /**
    *
    * @param parser
    * @param hasAccessTo
    * @param f
    * @tparam X
    * @return
    */
  def apiWithParserModel[X <: IndexedRow](parser: Reads[X] )( hasAccessTo: (X,Int) => Rep[Boolean] )(f: => models.User => X => Future[Result]) = {
    apiWithParser(parser) { user: models.User => parseResult: X =>
      db.run( hasAccessTo( parseResult, user.id.getOrElse( 0 ) ).result ).flatMap {
        case true =>
          f(user)(parseResult)
        case _ =>
          jsonErrorAccessDenied
      }(ec)
    }
  }

  def apiWithParserAccess[X <: IndexedRow](parser: Reads[X] )( hasAccessModel:UserHasAccess[X], access:ObjectAccess.Access )(f: => models.User => X => Future[Result]) = {
    apiWithParser(parser) { user: models.User => parseResult: X =>
      db.run( hasAccessModel.hasAccess( access )( LiteralColumn( parseResult.id.getOrElse(0) ) )( LiteralColumn( user.id.getOrElse( 0 ) ) ).result ).flatMap {
        case true =>
          f(user)(parseResult)
        case _ =>
          jsonErrorAccessDenied
      }(ec)
    }
  }



}
