package models.base

/**
  * User: aloise
  * Date: 23.05.16
  * Time: 18:53
  */
import javax.inject.Inject

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.dbio.Effect.Write
import slick.driver.JdbcProfile

import scala.concurrent.Future
import scala.reflect.ClassTag
import slick.lifted.Rep
import slick.model.Column
import slick.lifted.{CanBeQueryCondition, LiteralColumn, Tag}
import slick.profile.FixedSqlAction
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.H2Driver.api._

/**
  * User: aeon
  * Date: 13/03/16
  * Time: 19:13
  */

trait IndexedRow {
  def id: Option[Int] = None
}

abstract class IndexedTable[R] (tag:Tag, schema:String) extends BaseTable[R](tag, schema){

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

}

abstract class IndexedTableComponent[R <: IndexedRow, T <: IndexedTable[R]](override val records:TableQuery[T]) extends BaseTableComponent[R, T](records) {

  def insert(r:R): Future[Int] = {
    db.run(records returning records.map(_.id) += r)
  }

  def deleteById(id:Int):Future[Boolean] = {
    db.run( records.filter(_.id === id ).delete ).map( _ > 0 )
  }

  def findById(id:Int):Future[R] = db.run(records.filter(_.id === id).take(1).result).map{s =>
    s.headOption match {
      case Some(i) => i
      case None => throw new Exception("no_record_with_id")
    }
  }

  def findByIdOpt(id:Int):Future[Option[R]] =
    db.run(records.filter(_.id === id).take(1).result).map(s => s.headOption)

}
