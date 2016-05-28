package models.base


import play.api.db.slick.DatabaseConfigProvider
import slick.lifted._
import slick.driver.H2Driver.api._
import scala.concurrent.Future

/**
  * User: aloise
  * Date: 28.05.16
  * Time: 22:31
  */
class IndexedTableQuery[R <: IndexedRow, T <: IndexedTable[R]](builder: Tag => T) extends BaseTableQuery[R, T](builder) {

  def findById(id: Int) = {
    table.filter(_.id === id).result.head
  }

  def insert(record: R) = {
    table returning table.map(_.id) += record
  }

  def deleteById(id: Int) = {
    table.filter(_.id === id).delete
  }

  def update(id:Int, record:R) = {
    table.filter(_.id === id).update(record)
  }




}
