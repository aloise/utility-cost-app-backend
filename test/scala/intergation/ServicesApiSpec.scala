package intergation

/**
  * User: aloise
  * Date: 30.05.16
  * Time: 13:20
  */

import akka.stream.Materializer
import org.scalatestplus.play._
import play.api.cache.EhCacheModule
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws._
import play.api.mvc._
import play.api.test._
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.libs.functional.syntax._

class ServicesApiSpec extends PlaySpec with OneServerPerSuite {

  // Override app if you need an Application with other than
  // default parameters.
  implicit override lazy val app = new GuiceApplicationBuilder().disable[EhCacheModule].build()
  val wsClient = app.injector.instanceOf[WSClient]
  val address =  s"localhost:$port"
  val apiGateway = s"http://$address/api/"

  "server should return a homepage" in {

    val response = await(wsClient.url(s"http://$address/").get())

    response.status mustBe OK
  }

}
