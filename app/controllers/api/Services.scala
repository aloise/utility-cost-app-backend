package controllers.api

import javax.inject.Inject

import models.{PlacesServicesQuery, ServicesQuery}
import models.base.DBAccessProvider
import slick.driver.H2Driver.api._
import models.helpers.SlickColumnExtensions._
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

/**
  * User: aloise
  * Date: 29.05.16
  * Time: 14:43
  */
class Services @Inject() ( implicit ec:ExecutionContext, db: DBAccessProvider ) extends ApiController(ec, db) {

  import models.helpers.JsonModels._

  def list(placeId:Int) = apiWithAuth { user => r =>

    db.run {
      ( for {
        service <- ServicesQuery()
        servicePlace <- PlacesServicesQuery()
        if service.id === servicePlace.serviceId

      } yield service ).result
    } map { services =>
      jsonStatusOk( Json.obj("services" -> services ) )

    } recover {
      case ex:Throwable =>
        recoverJsonException(ex)
    }

  }

}
