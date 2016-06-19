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


  def hasServiceAccess( serviceId:Int ) = apiWithAuth( ServicesQuery.hasReadAccess( serviceId ) _ ){ user => r =>
      jsonStatusOkFuture( Json.obj("access" -> true ) )
  }


  def get( serviceId:Int ) = apiWithAuth( ServicesQuery.hasReadAccess( serviceId ) _ ){ user => r =>
        db.run( ServicesQuery.filter( s => ( s.id === serviceId ) && !s.isDeleted ).result.headOption ).map {
          case Some( service ) =>
            jsonStatusOk( Json.obj("service" -> service ) )
          case None =>
            recoverJsonErrors("not_found")
        }
  }

  def forPlace(placeId:Int ) = apiWithAuth{ user:models.User => r =>
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

  def update = apiWithParserModel( serviceToJson )( (s,uId) => ServicesQuery.hasWriteAccess( s.id.getOrElse(0) , uId ) ){ user => service =>
    db.run( ServicesQuery.filter( _.id === service.id.getOrElse(0) ).result.head ).flatMap { existingServiceData =>

        val updatedService = service.copy( isDeleted = false, createdByUserId = existingServiceData.createdByUserId )

        db.run( ServicesQuery.update( updatedService.id.getOrElse(0), updatedService ) ).map { _ =>
          jsonStatusOk( Json.obj( "service" -> service ) )
        }
    }
  }

  def delete(serviceId:Int) = apiWithAuth( ServicesQuery.hasWriteAccess( serviceId ) _ ){ user => r =>
        db.run( ServicesQuery.filter(_.id === serviceId).map(_.isDeleted).update(true) ).map { count =>
          jsonStatusOk
        }
    }


// it's allowed only for admin of the place
  def attachToPlace( serviceId:Int, placeId:Int ) = apiWithAuth( PlacesQuery.hasWriteAccess( placeId ) _ ) { user => r =>
    db.run( PlacesServicesQuery.insert( PlacesService( placeId, serviceId ) ) ).map { _ =>
      jsonStatusOk
    }
  }

  // it's allowed only for admin of the place
  def detachFromPlace( serviceId:Int, placeId:Int ) = apiWithAuth( PlacesQuery.hasWriteAccess( placeId ) _ ) { user => r =>
    db.run( PlacesServicesQuery.deleteFromPlace( placeId, serviceId ) ).map { _ =>
      jsonStatusOk
    }
  }

}
