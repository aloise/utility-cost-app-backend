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
class IndexedTableQuery[R <: IndexedRow,T <: IndexedTable[R] ]( builder: Tag => T ) extends BaseTableQuery[R,T]( builder ) {

  def insert()(implicit db:DBAccessProvider) = {

  }

  def findById(id:Int)(implicit db:DBAccessProvider):Future[R] = {
    findById(Some(id))(db)
  }

  def findById(idOpt:Option[Int])(implicit db:DBAccessProvider):Future[R] = {
    idOpt match {
      case Some(id) =>
        db.run(table.filter(_.id === id).result.head)
      case None =>
        Future.failed( new Exception("no_record_with_id") )
    }

  }

    def insert(record:R)(implicit db:DBAccessProvider): Future[Int] = {
      db.run(table returning table.map(_.id) += record)
    }

    def deleteById(id:Int)(implicit db:DBAccessProvider):Future[Int] = {
      db.run( table.filter(_.id === id ).delete )
    }


}
