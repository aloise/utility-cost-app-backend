package models

import java.time.LocalDateTime

import models.base.{IndexedRow, IndexedTable, IndexedTableQuery}
import models.helpers.SlickColumnExtensions._
import org.joda.money.{CurrencyUnit, Money}
import slick.driver.H2Driver.api._

/**
  * User: aloise
  * Date: 23.05.16
  * Time: 23:39
  */
case class Bill(
  override val id:Option[Int] = None,
  placeId:Int,
  rateId:Int,
  value:Money,
  created: LocalDateTime,
  paid:Option[LocalDateTime],
  override val isDeleted:Boolean = false
) extends IndexedRow


class BillsTable(tag:Tag) extends IndexedTable[Bill](tag, "bills") {

  def placeId = column[Int]("place_id")
  def rateId = column[Int]("rate_id")
  def created = column[LocalDateTime]("created")
  def paid = column[Option[LocalDateTime]]("paid")

  def valueAmount = column[BigDecimal]("value_amount")
  def valueCurrency = column[String]("value_currency")

  // def address = foreignKey("place_id",placeId,  )(_.id)

  def * = ( id.?, placeId, rateId, ( valueAmount, valueCurrency ), created, paid ).shaped <> (
    { case ( id, placeId, rateId, value, created, paid ) =>
      Bill( id, placeId, rateId, value, created, paid )
    },
    { bill:Bill =>
      Some( ( bill.id, bill.placeId, bill.rateId, ( BigDecimal(bill.value.getAmount), bill.value.getCurrencyUnit.getCode ), bill.created, bill.paid ) )
    }
  )

}

object BillsQuery extends IndexedTableQuery[Bill,BillsTable]( tag => new BillsTable(tag) ) {

}
