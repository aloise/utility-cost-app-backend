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

abstract class IndexedTable[R <: IndexedRow] (tag:Tag, schema:String) extends BaseTable[R](tag, schema){

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

}
