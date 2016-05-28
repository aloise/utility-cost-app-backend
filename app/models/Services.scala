package models

import java.time.LocalDateTime
import javax.inject.Inject
import models.base._
import models.helpers._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.Codecs
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsObject, JsValue}
import slick.driver.H2Driver.api._
import models.helpers.SlickColumnExtensions._



/**
  * User: aloise
  * Date: 23.05.16
  * Time: 22:18
  */
case class Service(
  override val id:Option[Int],
  title:String,
  area:String,
  description:String
) extends IndexedRow

class Services(tag:Tag) extends IndexedTable[Service](tag, "services") {

  def title = column[String]("title")
  def area = column[String]("area")
  def description = column[String]("description")

  def * = (id.?, title,area, description) <> ( Service.tupled, Service.unapply )

}

