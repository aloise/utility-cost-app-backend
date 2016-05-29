package models.helpers


import java.math.RoundingMode

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Json._
import play.api.libs.json.Reads._
import org.joda.money.{Money, CurrencyUnit }

import scala.util._
/**
  * User: aloise
  * Date: 29.05.16
  * Time: 15:10
  */
object JsonJodaMoney {


  class CurrencyUnitWrites extends Writes[CurrencyUnit]{
    override def writes(o: CurrencyUnit): JsValue = JsString( o.getCurrencyCode )
  }

  class CurrencyUnitReads extends Reads[CurrencyUnit]{
    override def reads(json: JsValue): JsResult[CurrencyUnit] = json match {
      case JsString( str ) => Try( CurrencyUnit.of( str ) ) match {
        case Success( unit ) => JsSuccess( unit )
        case _ => JsError("error.expected.currency")
      }
      case _ => JsError("error.expected.currency")
    }
  }


  class MoneyFormat( implicit currencyReads: Reads[CurrencyUnit], currencyWrites: Writes[CurrencyUnit] ) extends Format[Money] {
    protected val moneyJsonFormat = ((__ \ "currency").format[CurrencyUnit] and (__ \ "amount").format[BigDecimal]).tupled

    override def reads(json: JsValue): JsResult[Money] = moneyJsonFormat.reads(json) match {
      case JsSuccess((currency, amount), p) =>
        Try(Money.of(currency, amount.bigDecimal)) match {
          case Success(m) => JsSuccess(m, p)
          case _ => JsError("error.expected.money")
        }
      case e@JsError(_) =>
        e
    }

    override def writes(m: Money): JsObject = {
      moneyJsonFormat.writes( ( m.getCurrencyUnit, BigDecimal( m.getAmount ) ) )
    }

  }

  implicit val jodaCurrencyUnitJsonFormat = Format[CurrencyUnit]( new CurrencyUnitReads, new CurrencyUnitWrites )

  implicit val jodaMoneyJsonFormat = new MoneyFormat()


}
