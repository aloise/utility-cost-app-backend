package controllers.api

import javax.inject.Inject
import models._
import models.base.{DBAccessProvider, ObjectAccess}
import models.helpers.JsonModels._
import slick.driver.H2Driver.api._
import models.helpers.SlickColumnExtensions._
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

/**
  * User: aloise
  * Date: 31.05.16
  * Time: 22:59
  */
class Bills @Inject() ( implicit ec:ExecutionContext, db: DBAccessProvider ) extends ApiController(ec, db) {

  def get( billId:Int ) = apiWithAuth( BillsQuery.hasReadAccess( billId ) _ ) { user => r =>
        db.run( BillsQuery.filter( _.id === billId ).result.headOption ).map { bill =>
          jsonStatusOk(Json.obj( "bill" -> bill ))
        }
  }

}
