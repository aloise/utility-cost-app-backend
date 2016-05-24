package models

import java.time.LocalDateTime

import models.base.{IndexedRow, IndexedTable}
import models.helpers._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.Codecs
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsObject, JsValue}
import slick.driver.H2Driver.api._
import models.helpers.SlickColumnExtensions._
import org.joda.money.Money
import models.helpers.SlickColumnExtensions._

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
  paid:Option[LocalDateTime]
) extends IndexedRow

/*
class Bills(tag:Tag) extends IndexedTable[Bill](tag, "bills") {

  def placeId = column[Int]("place_id")
  def rateId = column[Int]("rate_id")
  def created = column[LocalDateTime]("created")
  def paid = column[Option[LocalDateTime]]("paid")

  def valueAmount = column[BigDecimal]("value_amount")
  def valueCurrency = column[String]("value_currency")

  def * = ( id.?, placeId, rateId, ( valueAmount, valueCurrency ), created, paid ) <> ( Bill.tupled, Bill.unapply ) (
    { case ( id, placeId, rateId, value, created, paid ) =>
      Bill( id, placeId, rateId, value, created, paid )
    },
    { case Bill( id, placeId, rateId, value, created, paid ) =>
      Some( ( id, placeId, rateId, value, created, paid ) )
    }
  )


}
*/

