package intergation

import java.time.LocalDateTime

import controllers.helpers.AuthAction
import models.{UserRole, UsersPlace, _}
import models.base.DBAccessProvider
import org.scalatest.{BeforeAndAfterAll, Suite}
import org.scalatestplus.play.{OneServerPerSuite, OneServerPerTest}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.test.Helpers._
import slick.jdbc.JdbcBackend.Database
import slick.driver.H2Driver.api._
import slick.driver.JdbcProfile
import play.api.test._
import org.scalatest._
import org.scalatestplus.play._
import play.api.{Play, Application}
import play.api.inject.guice._
import play.api.routing._

/**
  * User: aloise
  * Date: 30.05.16
  * Time: 14:29
  */
trait InitialSetup extends Suite with OneServerPerSuite {

  val db:DBAccessProvider = app.injector.instanceOf[DBAccessProvider]

  val address =  s"localhost:$port"
  val apiGateway = s"http://$address/api"

  val wsClient = app.injector.instanceOf[WSClient]
  val authAction = new AuthAction {}
  val appSalt = authAction.getSecretToken()( app.configuration )


  abstract override def run(testName: Option[String], args: Args): Status = {
    await( setupInitialData() )
    super.run(testName, args)
  }

  protected val dbSetup = DBIO.seq(
    ( UsersQuery.schema ++ UsersPlacesQuery.schema ++ PlacesQuery.schema ).create,
    UsersQuery ++= ( for( i <- 1 to 15 ) yield User( Some(i), "test-name-"+i, "test"+i+"@email.com", UsersQuery.passwordHash( "pass"+i, appSalt ), LocalDateTime.now() ) ),
    PlacesQuery ++= ( for( i <- 1 to 10 ) yield Place( Some(i), "Place "+i, "country"+i, "city"+i, "state"+i, "zip"+i, "address"+i ) ),
    UsersPlacesQuery ++= Seq( UsersPlace( 1, 1, UserRole.Admin ), UsersPlace( 1,2, UserRole.Admin ) , UsersPlace(1,3,UserRole.User) )
  )



  def setupInitialData() = {
    db run dbSetup
  }

}
