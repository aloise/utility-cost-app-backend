package models.helpers


import java.sql.Timestamp
import java.util.Date
import slick.driver.MySQLDriver.api._
import play.api.libs.json.{JsSuccess, Format, JsValue, JsNull}
import scala.reflect.ClassTag
import scala.util._
import slick.ast.{QueryParameter, LiteralNode}

/**
  * User: aloise
  * Date: 23.05.16
  * Time: 18:54
  */
class SlickColumnExtensions {

  implicit val JsValueColumnType =
    MappedColumnType.base[JsValue, String](
      json => play.api.libs.json.Json.stringify( json ),
      str => Try(play.api.libs.json.Json.parse(str)).getOrElse(JsNull)
    )

  def jsBasedColumnType[T: ClassTag](implicit f: Format[T]) =
    MappedColumnType.base[T, String](
      json => play.api.libs.json.Json.stringify( f.writes(json) ),
      str =>
        Try(f.reads(play.api.libs.json.Json.parse(str))).map {
          case JsSuccess(value, _) => value
          case _ => null
        }.getOrElse(null).asInstanceOf[T]

    )

}
