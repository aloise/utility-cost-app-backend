package models


import java.time.LocalDateTime
import javax.inject.Inject

import models.base._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.Codecs
import play.api.libs.concurrent.Execution.Implicits.defaultContext
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
     created: LocalDateTime = LocalDateTime.now(),
     override val isDeleted:Boolean = false
) extends IndexedRow

class UsersTable(tag: Tag) extends IndexedTable[User](tag, "USERS") {

  def name = column[String]("name")

  def email = column[String]("email")

  def password = column[String]("password")

  def created = column[LocalDateTime]("created")

  def * = (id.?, name, email, password, created, isDeleted) <> (User.tupled, User.unapply)

}

object UsersQuery extends IndexedTableQuery[User,UsersTable]( tag => new UsersTable(tag) ) {

  def passwordHash( password:String, salt:String ) = {
    Codecs.sha1( salt + password )
  }

  def signup(userToInsert: User, salt:String) = {
    insert(userToInsert.copy(password = passwordHash(userToInsert.password, salt)))
//    db.run( UsersQuery().filter(_.email === userToInsert.email).result.headOption ).flatMap {
//      case Some(_) =>
//        Future.failed(throw new Exception("email_not_available"))
//      case None =>
//        insert(userToInsert.copy(password = passwordHash(userToInsert.password, salt))).map { newUserId =>
//          val newUser = userToInsert.copy(id = Some(newUserId))
//          newUser
//        }
//    }
  }

  def authenticate(email: String, password: String, salt:String)(implicit db:DBAccessProvider) = {
    db.run {
      this.filter{
        u => !u.isDeleted && (u.email === email) && (u.password === passwordHash(password, salt))
      }.result.headOption
    }
  }


}
