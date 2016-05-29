package controllers.api

import javax.inject.Inject

import models._
import models.base.DBAccessProvider
import models.helpers.JsonModels
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
        service <- ServicesQuery
        servicePlace <- PlacesServicesQuery
        if service.id === servicePlace.serviceId

      } yield service ).result
    } map { services =>
      jsonStatusOk( Json.obj("services" -> services ) )
    }

  }

  def create = apiWithParser( JsonModels.serviceToJson ) { user => service =>
    db.run(ServicesQuery.insert(service.copy( id = None )).flatMap { newId =>
      ServicesQuery.findById(newId)
    }).map { place =>
      jsonStatusOk(Json.obj("service" -> Json.toJson(place)))
    }

  }

  def update() = apiWithParser( serviceToJson ) { user => service =>
    jsonStatusOkFuture()
  }


}
