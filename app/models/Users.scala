package models


import java.time.LocalDateTime
import javax.inject.Inject

import models.base.{IndexedRow, IndexedTable, IndexedTableComponent}
import models.helpers._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.Codecs
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsObject, JsValue}
import slick.driver.H2Driver.api._
import models.helpers.SlickColumnExtensions._

import scala.concurrent.Future
/**
  * User: aloise
  * Date: 23.05.16
  * Time: 18:32
  */

case class User(
                 override val id: Option[Int] = None,
                 name: String,
                 email: String,
                 password: String,
                 created: LocalDateTime
               ) extends IndexedRow

class UsersTable(tag: Tag) extends IndexedTable[User](tag, "users") {

  def name = column[String]("name")

  def email = column[String]("email")

  def password = column[String]("password")

  def created = column[LocalDateTime]("created")

  def * = (id.?, name, email, password, created) <> (User.tupled, User.unapply)

}

class Users @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends IndexedTableComponent[User, UsersTable](slick.lifted.TableQuery[UsersTable]) {

  def signup(userToInsert: User): Future[User] = {
    findWhere(_.email === userToInsert.email).flatMap {
      case Some(_) => Future.failed(throw new Exception("email_not_available"))
      case None =>
        insert(userToInsert.copy(password = Codecs.sha1(userToInsert.password))).map { newUserId =>
          val newUser = userToInsert.copy(id = Some(newUserId))
          newUser
        }
    }
  }

  def authenticate(email: String, password: String) = {
    val saltPassword = Codecs.sha1(password)
    findWhere(u => u.email === email && u.password === saltPassword)
  }

}
