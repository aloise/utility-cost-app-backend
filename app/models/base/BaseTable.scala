package models.base

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.dbio.Effect.Write
import slick.driver.JdbcProfile

import scala.concurrent.Future
import scala.reflect.ClassTag
import slick.driver.H2Driver.api._
import slick.lifted.Rep
import slick.model.Column
import slick.lifted.{CanBeQueryCondition, LiteralColumn, Tag}
import slick.profile.FixedSqlAction
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * User: aloise
  * Date: 23.05.16
  * Time: 21:00
  */
abstract class BaseTable[R] (tag:Tag, schema:String) extends Table[R](tag, schema){

}


abstract class BaseTableComponent[R, T <: BaseTable[R]](val records:TableQuery[T]) extends HasDatabaseConfigProvider[JdbcProfile] {

  def all():Future[Seq[R]] = db.run(records.result)

  def count[C <: Rep[_]](f: (T) => C)(implicit wt: CanBeQueryCondition[C]) = db.run(records.filter(f).length.result)

  def filter[C <: Rep[_]](f: (T) => C)(implicit wt: CanBeQueryCondition[C]):Future[Seq[R]] = db.run(records.filter(f).result)

  def findWhere[C <: Rep[_]](f: (T) => C)(implicit wt: CanBeQueryCondition[C]) = db.run(records.filter(f).take(1).result).map(_.headOption)

}