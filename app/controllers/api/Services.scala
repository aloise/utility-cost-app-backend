package controllers.api

import javax.inject.Inject

import models._
import models.base.{DBAccessProvider, ObjectAccess}
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
        if service.id === servicePlace.serviceId && !service.isDeleted

      } yield service ).result
    } map { services =>
      jsonStatusOk( Json.obj("services" -> services ) )
    }

  }

  def create = apiWithParser( JsonModels.serviceToJson ) { user => service =>
    db.run(ServicesQuery.insert(service.copy( id = None, createdByUserId = user.id.getOrElse(0) )).flatMap { newId =>
      ServicesQuery.findById(newId)
    }).map { service =>
      jsonStatusOk(Json.obj("service" -> service ))
    }

  }

  def update() = apiWithParser( serviceToJson ) { user => service =>
    db.run( ServicesQuery.hasAccess( user.id.getOrElse(0), service.id.getOrElse(0) ).result ).flatMap {
      case true =>
        db.run( ServicesQuery.update( service ) ).map { _ =>
          jsonStatusOk( Json.obj( "service" -> service ) )
        }

      case _ =>
        recoverJsonErrorsFuture("access_denied")
    }


  }

  def delete(serviceId:Int) = apiWithAuth { user => r =>
    db.run( ServicesQuery.hasAccess( user.id.getOrElse(0), serviceId ).result ).flatMap {
      case true =>
        // db.run( ServicesQuery.filter(_.id === serviceId). ) )
        jsonStatusOkFuture()
      case _ =>
        recoverJsonErrorsFuture("access_denied")
    }
  }


  def attachToPlace( serviceId:Int, placeId:Int ) = apiWithAuth { user => r =>
    // it's allowed only for admin of the place
    db.run( PlacesQuery.hasAccess( placeId, user.id.getOrElse(0) ).result ).flatMap {
      case true =>
        jsonStatusOkFuture()
      case _ =>
        recoverJsonErrorsFuture("access_denied")
    }
  }

  def detachFromPlace( serviceId:Int, placeId:Int ) = apiWithAuth { user => r =>
    // it's allowed only for admin of the place

    db.run( PlacesQuery.hasAccess( placeId, user.id.getOrElse(0) ).result ).flatMap {
      case true =>
        // PlacesServicesQuery.filter( ps => ( ps. ) )
        jsonStatusOkFuture()
      case _ =>
        recoverJsonErrorsFuture("access_denied")
    }
  }

}
