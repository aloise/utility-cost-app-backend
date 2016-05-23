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
import slick.driver.MySQLDriver.api._
import slick.lifted.Rep
import slick.model.Column
import slick.lifted.{CanBeQueryCondition, LiteralColumn, Tag}
import slick.profile.FixedSqlAction

import scala.concurrent.ExecutionContext.Implicits.global


/**
  * User: aeon
  * Date: 13/03/16
  * Time: 19:13
  */

trait IndexedRow {
  def id: Option[Int] = None
}

abstract class IndexedTableComponent[R <: IndexedRow with Serializable with Object , T <: IndexedTable[R]](override val records:TableQuery[T]) extends BaseTableComponent[R, T](records) {

  def insert(r:R): Future[Int] = {
    db.run(records returning records.map(_.id) += r)
  }

  def findById(id:Int):Future[R] = db.run(records.filter(_.id === id).take(1).result).map{s =>
    s.headOption match {
      case Some(i) => i
      case None => throw new Exception("no_record_with_id")
    }
  }

  def findByIdOpt(id:Int):Future[Option[R]] = db.run(records.filter(_.id === id).take(1).result).map(s => s.headOption)

}

abstract class IndexedTable[R] (tag:Tag, schema:String) extends Table[R](tag, schema){

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

}

abstract class BaseTableComponent[R, T <: Table[R]](val records:TableQuery[T]) extends HasDatabaseConfigProvider[JdbcProfile] {

  def all():Future[Seq[R]] = db.run(records.result)

  def count[C <: Rep[_]](f: (T) => C)(implicit wt: CanBeQueryCondition[C]) = db.run(records.filter(f).length.result)

  def filter[C <: Rep[_]](f: (T) => C)(implicit wt: CanBeQueryCondition[C]):Future[Seq[R]] = db.run(records.filter(f).result)

  def findWhere[C <: Rep[_]](f: (T) => C)(implicit wt: CanBeQueryCondition[C]) = db.run(records.filter(f).take(1).result).map(_.headOption)

}