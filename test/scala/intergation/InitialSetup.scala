package intergation

import java.time.LocalDateTime

import models.{UserRole, UsersPlace, _}
import models.base.DBAccessProvider
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcBackend.Database
import slick.driver.H2Driver.api._
import slick.driver.JdbcProfile

/**
  * User: aloise
  * Date: 30.05.16
  * Time: 14:29
  */
trait InitialSetup {

  val db:DBAccessProvider

  protected val dbSetup = DBIO.seq(
    ( UsersQuery.schema ++ UsersPlacesQuery.schema ++ PlacesQuery.schema ).create,
    UsersQuery ++= ( for( i <- 1 to 15 ) yield User( Some(i), "test-name-"+i, "test"+i+"@email.com", "pass"+i, LocalDateTime.now() ) ),
    PlacesQuery ++= ( for( i <- 1 to 10 ) yield Place( Some(i), "Place "+i, "country"+i, "city"+i, "state"+i, "zip"+i, "address"+i ) ),
    UsersPlacesQuery ++= Seq( UsersPlace( 1, 1, UserRole.Admin ), UsersPlace( 1,2, UserRole.Admin ) , UsersPlace(1,3,UserRole.User) )
  )

  def setupInitialData() = {
    db run dbSetup
  }

}
