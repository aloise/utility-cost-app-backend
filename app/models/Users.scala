package models

import scala.concurrent.Future
import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile

/**
  * User: aloise
  * Date: 23.05.16
  * Time: 18:32
  */

case class User( id:Int, name:String, email:String, password:String )

class Users @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val Cats = TableQuery[CatsTable]

  def all(): Future[Seq[User]] = db.run(Cats.result)

  def insert(cat: User): Future[Unit] = db.run(Cats += cat).map { _ => () }

  private class CatsTable(tag: Tag) extends Table[User](tag, "CAT") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def email = column[String]("email")
    def password = column[String]("password")

    def * = (id, name, email, password) <> (User.tupled, User.unapply)
  }

}
