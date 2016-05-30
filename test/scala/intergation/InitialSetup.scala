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
import play.api.{Application, Play}
import play.api.inject.guice._
import play.api.libs.json.Json
import play.api.routing._

import scala.collection.immutable.IndexedSeq

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

  def authHeaders( authToken:String ) = Seq( "Auth-Token" -> authToken )


  abstract override def run(testName: Option[String], args: Args): Status = {
    await( setupInitialData() )
    super.run(testName, args)
  }

  val users: IndexedSeq[User] = for (i <- 1 to 15) yield User(Some(i), "test-name-" + i, "test" + i + "@email.com", UsersQuery.passwordHash("pass" + i, appSalt))
  val places: IndexedSeq[Place] = for (i <- 1 to 10) yield Place(Some(i), "Place " + i, "country" + i, "city" + i, "state" + i, "zip" + i, "address" + i)

  protected val dbSetup = DBIO.seq(
    ( UsersQuery.schema ++ UsersPlacesQuery.schema ++ PlacesQuery.schema ++ ServicesQuery.schema ++ PlacesServicesQuery.schema ++ BillsQuery.schema ++ ServiceRatesQuery.schema ).create,
    UsersQuery ++= users,
    PlacesQuery ++= places,
    ServicesQuery ++= ( for( i <- 1 to 10 ) yield Service( Some(i), "Service "+i, "Area"+i, "Description"+i, createdByUserId = i % 2 ) ),
    PlacesServicesQuery ++= ( for( i <- 1 to 10 ) yield PlacesService( i, i ) ),
    PlacesServicesQuery ++= Seq( PlacesService( 1, 2 ), PlacesService( 1, 3 ) ),
    UsersPlacesQuery ++= Seq( UsersPlace( 1, 1, UserRole.Admin ), UsersPlace( 1,2, UserRole.Admin ) , UsersPlace(1,3,UserRole.User), UsersPlace(2,1,UserRole.User), UsersPlace(2,2,UserRole.User), UsersPlace(2,5,UserRole.Admin) ),
    ServiceRatesQuery ++= Seq( ServiceRate( Some(1), 1, true ), ServiceRate( Some(2), 1, false, LocalDateTime.now().minusDays(30), Some( LocalDateTime.now() ) ) , ServiceRate( Some(3), 2, true ) )
  )

  def setupInitialData() = {
    db run dbSetup
  }

  def auth(userTestEmail:String, userTestPassword:String) = {
    val requestBody = Json.obj("email" -> userTestEmail, "password" -> userTestPassword )
    val response = await(wsClient.url(s"$apiGateway/users/auth").post(requestBody))
    val js = Json.parse(response.body)

    val newToken = (js \ "token").asOpt[String]

    newToken.getOrElse("")
  }

}
