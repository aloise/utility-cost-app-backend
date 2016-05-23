package models

import java.sql.Timestamp
import java.util.Date
import javax.inject.Inject

import models.base.{IndexedRow, IndexedTable}
import models.helpers._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.Codecs
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsObject, JsValue}
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future
/**
  * User: aloise
  * Date: 23.05.16
  * Time: 18:32
  */

case class User( override val id:Option[Int], name:String, email:String, password:String ) extends IndexedRow

class Users(tag:Tag) extends IndexedTable[User](tag, "users") {

    def name = column[String]("name")
    def email = column[String]("email")
    def password = column[String]("password")

    def * = (id.?, name, email, password) <> (User.tupled, User.unapply)

}
