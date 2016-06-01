package controllers.api

import java.time.LocalDateTime
import javax.inject.Inject

import models._
import models.base.{DBAccessProvider, ObjectAccess}
import models.helpers.JsonModels
import models.helpers.JsonModels._
import slick.driver.H2Driver.api._
import models.helpers.SlickColumnExtensions._
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext
import scala.util.Try

/**
  * User: aloise
  * Date: 31.05.16
  * Time: 22:59
  */
class Bills @Inject() ( implicit ec:ExecutionContext, db: DBAccessProvider ) extends ApiController(ec, db) {

  val defaultDate = LocalDateTime.of(2000, 1, 1, 1, 1, 1 )

  def get( billId:Int ) = apiWithAuth( BillsQuery.hasReadAccess( billId ) _ ) { user => r =>
      db.run( BillsQuery.filter( _.id === billId ).result.headOption ).map { bill =>
        jsonStatusOk(Json.obj( "bill" -> bill ))
      }
  }

  def listByService( serviceId:Int, fromDate:String = "" ) = apiWithAuth( ServicesQuery.hasReadAccess( serviceId ) _ ) { user => r =>
    val date = Try( LocalDateTime.parse(fromDate) ).getOrElse( defaultDate )

    db.run( BillsQuery.filter( b => ( b.serviceId === serviceId ) && ( b.created >= date ) && !b.isDeleted ).result ).map { bills =>
      jsonStatusOk( Json.obj("bills" -> bills ) )
    }
  }

  def listByServiceRate( serviceRateId:Int, fromDate:String = "" ) = apiWithAuth( ServiceRatesQuery.hasReadAccess( serviceRateId ) _ ) { user => r =>
    val date = Try( LocalDateTime.parse(fromDate) ).getOrElse( defaultDate )

    db.run( BillsQuery.filter( b => ( b.serviceRateId === serviceRateId ) && ( b.created >= date ) && !b.isDeleted ).result ).map { bills =>
      jsonStatusOk( Json.obj("bills" -> bills ) )
    }
  }

  def listByPlace( placeId:Int, fromDate:String = "" ) = apiWithAuth( PlacesQuery.hasReadAccess( placeId ) _ ) { user => r =>
    val date = Try( LocalDateTime.parse(fromDate) ).getOrElse( defaultDate )

    db.run( BillsQuery.filter( b => ( b.placeId === placeId ) && ( b.created >= date ) && !b.isDeleted ).result ).map { bills =>
      jsonStatusOk( Json.obj("bills" -> bills ) )
    }
  }

  def create = apiWithParserModel( JsonModels.billToJson )( ( bill, uId ) => PlacesQuery.hasWriteAccess( bill.serviceId )(uId) ){ user => bill =>

    val billData = bill.copy( id = None, isDeleted = false )

    db.run(BillsQuery.insert(billData)).flatMap( id => db.run( BillsQuery.findById(id) ) ).map { data =>
      jsonStatusOk(Json.obj( "bill" -> data ))
    }
  }

  def update = apiWithParserModel( JsonModels.billToJson ){ ( bill, uId ) =>
      PlacesQuery.hasWriteAccess( bill.placeId )(uId) &&
      ServicesQuery.hasWriteAccess( bill.serviceId )(uId) &&
      ServiceRatesQuery.hasWriteAccess( bill.serviceRateId )(uId)
    }{ user:models.User => bill:models.Bill =>
      db.run( BillsQuery.filter( _.id === bill.id.getOrElse(0) ).result.headOption ).flatMap {
        case Some( existingBillData ) =>

          db.run( BillsQuery.update( bill.id.getOrElse(0), bill ) ).map { _ =>
            jsonStatusOk( Json.obj( "bill" -> bill ) )
          }

        case _ =>
          jsonErrorAccessDenied
      }
    }

  def delete( billId:Int ) = apiWithAuth( BillsQuery.hasWriteAccess(billId) _ ) { user => r =>
    db.run( BillsQuery.filter( _.id === billId ).map(_.isDeleted).update( true ) ).map { _ =>
      jsonStatusOk
    }
  }



}
