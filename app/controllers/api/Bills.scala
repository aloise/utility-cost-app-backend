package controllers.api

import java.time.LocalDateTime
import javax.inject.Inject

import models._
import models.base.{DBAccessProvider, ObjectAccess}
import models.helpers.JsonModels
import models.helpers.JsonModels._
import slick.driver.H2Driver.api._
import models.helpers.SlickColumnExtensions._
import org.joda.money.Money
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * User: aloise
  * Date: 31.05.16
  * Time: 22:59
  */
class Bills @Inject() ( implicit ec:ExecutionContext, db: DBAccessProvider ) extends ApiController(ec, db) {

  val defaultDate = LocalDateTime.of(2000, 1, 1, 1, 1, 1 )
  val defaultEndDate = LocalDateTime.of(2999, 1, 1, 1, 1, 1 )

  def get( billId:Int ) = apiWithAuth( BillsQuery.hasReadAccess( billId ) _ ) { user => r =>
      db.run( BillsQuery.filter( _.id === billId ).result.headOption ).map { bill =>
        jsonStatusOk(Json.obj( "bill" -> bill ))
      }
  }

  def listByService( serviceId:Int, fromDateStr:String = "", toDateStr:String = "" ) = apiWithAuth( ServicesQuery.hasReadAccess( serviceId ) _ ) { user => r =>
    val fromDate = Try( LocalDateTime.parse(fromDateStr) ).getOrElse( defaultDate )
    val toDate = Try( LocalDateTime.parse(toDateStr) ).getOrElse( defaultEndDate )

    db.run( BillsQuery.filter( b => ( b.serviceId === serviceId ) && ( b.created >= fromDate ) && ( b.created < toDate ) && !b.isDeleted ).result ).map { bills =>
      jsonStatusOk( Json.obj("bills" -> bills ) )
    }
  }

  def listByServiceRate( serviceRateId:Int, fromDateStr:String = "", toDateStr:String = "" ) = apiWithAuth( ServiceRatesQuery.hasReadAccess( serviceRateId ) _ ) { user => r =>
    val fromDate = Try( LocalDateTime.parse(fromDateStr) ).getOrElse( defaultDate )
    val toDate = Try( LocalDateTime.parse(toDateStr) ).getOrElse( defaultEndDate )

    db.run( BillsQuery.filter( b => ( b.serviceRateId === serviceRateId ) && ( b.created >= fromDate ) && ( b.created < toDate )  && !b.isDeleted ).result ).map { bills =>
      jsonStatusOk( Json.obj("bills" -> bills ) )
    }
  }

  def listByPlace( placeId:Int, fromDateStr:String = "", toDateStr:String = "" ) = apiWithAuth( PlacesQuery.hasReadAccess( placeId ) _ ) { user => r =>
    val fromDate = Try( LocalDateTime.parse(fromDateStr) ).getOrElse( defaultDate )
    val toDate = Try( LocalDateTime.parse(toDateStr) ).getOrElse( defaultEndDate )

    db.run( BillsQuery.filter( b => ( b.placeId === placeId ) && ( b.created >= fromDate )  && ( b.created < toDate )  && !b.isDeleted ).result ).map { bills =>
      jsonStatusOk( Json.obj("bills" -> bills ) )
    }
  }

  def create( updateAmount:Int = 0 ) = apiWithParserModel( JsonModels.billToJson )( ( bill, uId ) => PlacesQuery.hasWriteAccess( bill.serviceId )(uId) ){ user => bill =>
    val updatedAmountValue =
      if( updateAmount > 0 )
        getBillAmount( bill.created, bill.readout ) recover { case _:Throwable => bill.value }
      else
        Future.successful( bill.value )

    updatedAmountValue.flatMap { billAmount =>

      val billData = bill.copy( id = None, isDeleted = false, value = billAmount )

      db.run(BillsQuery.insert(billData)).flatMap( id => db.run( BillsQuery.findById(id) ) ).map { data =>
        jsonStatusOk(Json.obj( "bill" -> data ))
      }
    }
  }

  def update( updateAmount:Int = 0 ) = apiWithParserModel( JsonModels.billToJson ){ ( bill, uId ) =>
      PlacesQuery.hasWriteAccess( bill.placeId )(uId) &&
      ServicesQuery.hasWriteAccess( bill.serviceId )(uId) &&
      ServiceRatesQuery.hasWriteAccess( bill.serviceRateId )(uId)
    }{ user:models.User => bill:models.Bill =>

      val updatedAmountValue =
        if( updateAmount > 0 )
          getBillAmount( bill.created, bill.readout ) recover { case _:Throwable => bill.value }
        else
          Future.successful( bill.value )

      updatedAmountValue.flatMap { billAmount =>

        db.run(BillsQuery.filter(_.id === bill.id.getOrElse(0)).result.headOption).flatMap {
          case Some(existingBillData) =>

            val billData = bill.copy( value = billAmount )

            db.run(BillsQuery.update(bill.id.getOrElse(0), billData)).map { _ =>
              jsonStatusOk(Json.obj("bill" -> bill))
            }

          case _ =>
            jsonErrorAccessDenied
        }
      }
    }

  def delete( billId:Int ) = apiWithAuth( BillsQuery.hasWriteAccess(billId) _ ) { user => r =>
    db.run( BillsQuery.filter( _.id === billId ).map(_.isDeleted).update( true ) ).map { _ =>
      jsonStatusOk
    }
  }

  protected def getBillAmount( currentMonthDate:LocalDateTime, thisMonthReadout: BigDecimal):Future[Money] = {

    // find last previous month bill

    val previousMonthStart = currentMonthDate.minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
    val previousMonthEnd = previousMonthStart.plusMonths(1).minusNanos(1)

    db.run( BillsQuery.lastBillWithinPeriod( previousMonthStart, previousMonthEnd ) ).map {
      case Some( ( lastBill, serviceRate ) ) =>
        serviceRate.calculateAmount( lastBill.readout, thisMonthReadout )

      case _ =>
        throw new IllegalArgumentException()
    }

  }

}
