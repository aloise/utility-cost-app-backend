package models.helpers


import java.sql.Timestamp
import java.util.Date

import slick.driver.MySQLDriver.api._
import play.api.libs.json.{Format, JsNull, JsSuccess, JsValue}

import scala.reflect.ClassTag
import slick.ast.{LiteralNode, QueryParameter}
import java.time.{LocalDateTime, ZoneId}
import java.util

import org.joda.money._

import scala.language.implicitConversions
import scala.util.Try


/**
  * User: aloise
  * Date: 23.05.16
  * Time: 18:54
  */
object SlickColumnExtensions {

  implicit val JsValueColumnType =
    MappedColumnType.base[JsValue, String](
      json => play.api.libs.json.Json.stringify( json ),
      str => Try(play.api.libs.json.Json.parse(str)).getOrElse(JsNull)
    )

  implicit val LocalDateTimeColumnType =
    MappedColumnType.base[LocalDateTime, Timestamp](
      dt => Timestamp.valueOf( dt ),
      ts => ts.toLocalDateTime
    )

  implicit def jodaMoney2Tuple( tuple:( BigDecimal,String ) ):Money = {
      Money.of( CurrencyUnit.of( tuple._2 ), tuple._1.bigDecimal )
  }

  implicit def tuple2JodaMoney( m:Money ):( BigDecimal,String ) = {
    ( m.getAmount, m.getCurrencyUnit.getCode )
  }

  def enumColumnType[T <: scala.Enumeration]( enum:T ) =
    MappedColumnType.base[T#Value, String](
      value   => value.toString,
      str     => enum.withName( str )
    )

  implicit def jsBasedColumnType[T: ClassTag](implicit f: Format[T]) =
    MappedColumnType.base[T, String](
      json => play.api.libs.json.Json.stringify( f.writes(json) ),
      str =>
        Try(f.reads(play.api.libs.json.Json.parse(str))).map {
          case JsSuccess(value, _) => value
          case _ => null
        }.getOrElse(null).asInstanceOf[T]

    )

}
