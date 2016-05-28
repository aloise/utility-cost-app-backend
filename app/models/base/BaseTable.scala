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
