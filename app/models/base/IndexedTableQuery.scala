package models.base


import play.api.db.slick.DatabaseConfigProvider
import slick.collection.heterogeneous.Zero
import slick.lifted.{TableQuery, _}
import slick.driver.H2Driver.api._

import scala.concurrent.Future

/**
  * User: aloise
  * Date: 28.05.16
  * Time: 22:31
  */
class IndexedTableQuery[R <: IndexedRow, T <: IndexedTable[R]](builder: Tag => T) extends BaseTableQuery[R, T](builder) {

  def findById(id: Int) = {
    filter(r => r.id === id && !r.isDeleted).result.headOption
  }

  override def insert(record: R) = {
     this returning map(_.id) += record
  }

  def deleteById(id: Int) = {
    filter(_.id === id).map(_.isDeleted).update(true)
  }

  def update(id:Int, record:R) = {
    filter(r => r.id === id && !r.isDeleted).update(record)
  }




}
