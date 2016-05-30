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


  def get( serviceId:Int ) = apiWithAuth{ user => r =>
    db.run( ServicesQuery.hasAccess( user.id.getOrElse(0), serviceId, ObjectAccess.Read ).result ).flatMap {
      case true =>
        db.run( ServicesQuery.filter( s => ( s.id === serviceId ) && !s.isDeleted ).result.headOption ).map {
          case Some( service ) =>
            jsonStatusOk( Json.obj("service" -> service ) )
          case None =>
            recoverJsonErrors("not_found")
        }
      case _ =>
        recoverJsonErrorsFuture("access_denied")
    }
  }

  def forPlace(placeId:Int) = apiWithAuth { user => r =>
    db.run {
      ServicesQuery.listByPlace( user.id.getOrElse(0), placeId ).result
    } map { services =>
      jsonStatusOk( Json.obj("services" -> services ) )
    }
  }



  def create = apiWithParser( JsonModels.serviceToJson ) { user => service =>
    db.run(ServicesQuery.insert(service.copy( id = None, createdByUserId = user.id.getOrElse(0), isDeleted = false )).flatMap { newId =>
      ServicesQuery.findById(newId)
    }).map { service =>
      jsonStatusOk(Json.obj("service" -> service ))
    }

  }

  def update = apiWithParser( serviceToJson ) { user => service =>
    db.run( ServicesQuery.hasAccess( user.id.getOrElse(0), service.id.getOrElse(0) ).result ).flatMap {
      case true =>
        db.run( ServicesQuery.update( service.copy( isDeleted = false ) ) ).map { _ =>
          jsonStatusOk( Json.obj( "service" -> service ) )
        }

      case _ =>
        recoverJsonErrorsFuture("access_denied")
    }
  }

  def delete(serviceId:Int) = apiWithAuth { user => r =>
    db.run( ServicesQuery.hasAccess( user.id.getOrElse(0), serviceId ).result ).flatMap {
      case true =>
        db.run( ServicesQuery.filter(_.id === serviceId).map(_.isDeleted).update(true) ).map { count =>
          jsonStatusOk
        }
      case _ =>
        recoverJsonErrorsFuture("access_denied")
    }
  }


  def attachToPlace( serviceId:Int, placeId:Int ) = apiWithAuth { user => r =>
    // it's allowed only for admin of the place
    db.run( PlacesQuery.hasAccess( placeId, user.id.getOrElse(0) ).result ).flatMap {
      case true =>
        db.run( PlacesServicesQuery.insert( PlacesService( placeId, serviceId ) ) ).map { _ =>
          jsonStatusOk
        }
      case _ =>
        recoverJsonErrorsFuture("access_denied")
    }
  }

  def detachFromPlace( serviceId:Int, placeId:Int ) = apiWithAuth { user => r =>
    // it's allowed only for admin of the place

    db.run( PlacesQuery.hasAccess( placeId, user.id.getOrElse(0) ).result ).flatMap {
      case true =>
        db.run( PlacesServicesQuery.deleteFromPlace( placeId, serviceId ) ).map { _ =>
          jsonStatusOk
        }

      case _ =>
        recoverJsonErrorsFuture("access_denied")
    }
  }

}
