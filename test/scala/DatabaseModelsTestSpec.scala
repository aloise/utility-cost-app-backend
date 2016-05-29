
import java.time.LocalDateTime

import akka.util.Timeout
import models._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcBackend.Database
import slick.driver.H2Driver.api._
import slick.driver.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration._


/**
  * User: aloise
  * Date: 28.05.16
  * Time: 10:35
  */
class DatabaseModelsTestSpec extends PlaySpec with ScalaFutures with BeforeAndAfterAll with IntegrationPatience {

  // default test table
  val db = Database.forConfig("h2mem1")

  val setup = DBIO.seq(
    ( UsersQuery.schema ++ UsersPlacesQuery.schema ++ PlacesQuery.schema ).create,
    UsersQuery ++= ( for( i <- 1 to 15 ) yield User( Some(i), "test-name-"+i, "test"+i+"@email.com", "pass"+i, LocalDateTime.now() ) ),
    PlacesQuery ++= ( for( i <- 1 to 10 ) yield Place( Some(i), "Place "+i, "country"+i, "city"+i, "state"+i, "zip"+i, "address"+i ) ),
    UsersPlacesQuery ++= Seq( UsersPlace( 1, 1, UserRole.Admin ), UsersPlace( 1,2, UserRole.Admin ) , UsersPlace(1,3,UserRole.User) )
  )

  "Database" must {

    "create the scheme" in {

      whenReady( db.run(setup) ){ result =>
        result mustBe( () )
      }

    }

    "return the list of places for user" in {
      whenReady( db.run( PlacesQuery.forUser(1).result ) ){ items =>
        items.length mustBe 3
        // users
        items.map(_._1.userId) must contain only ( 1 )
        // places
        items.flatMap(_._2.id) must contain only ( 1,2,3 )


      }

    }
  }

  override def afterAll = {

    db.close()
  }


}
