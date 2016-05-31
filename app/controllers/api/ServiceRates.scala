package controllers.api

import java.time.LocalDateTime

import models._
import models.base.{DBAccessProvider, ObjectAccess}
import models.helpers.JsonModels._
import slick.driver.H2Driver.api._
import models.helpers.SlickColumnExtensions._
import play.api.libs.json.Json
import javax.inject.Inject

import models.helpers.JsonModels

import scala.concurrent.ExecutionContext

/**
  * User: aloise
  * Date: 30.05.16
  * Time: 22:27
  */
class ServiceRates @Inject()(implicit ec:ExecutionContext, db: DBAccessProvider ) extends ApiController(ec, db) {


  def listForService( serviceId:Int, includeInactive:Int = 0 ) = apiWithAuth { user => r =>
    db.run( ServicesQuery.hasAccess( user.id.getOrElse(0), serviceId, ObjectAccess.Read ).result ).flatMap {
      case true =>
        db.run( ServiceRatesQuery.forService( serviceId, includeInactive != 0 ).result ).map { serviceRates =>
          jsonStatusOk(Json.obj("serviceRates" -> serviceRates ))

        }
      case _ =>
        jsonErrorAccessDenied
    }

  }

  def get( rateId:Int ) = apiWithAuth { user => r =>
    db.run( ServiceRatesQuery.hasAccess( user.id.getOrElse(0), rateId, ObjectAccess.Read ).result ).flatMap {
      case true =>
        db.run( ServiceRatesQuery.filter( _.id === rateId ).result.headOption ).map { serviceRate =>
          jsonStatusOk(Json.obj("serviceRate" -> serviceRate ))
        }

      case _ =>
        jsonErrorAccessDenied
    }
  }

  def create = apiWithParser( JsonModels.serviceRatesToJson ) { user => serviceRate =>
    db.run( ServicesQuery.hasAccess( user.id.getOrElse(0), serviceRate.serviceId, ObjectAccess.Write ).result ).flatMap {
      case true =>
        val serviceRateData = serviceRate.copy( id = None, isDeleted = false )

        db.run(ServiceRatesQuery.insert(serviceRateData).flatMap { newId =>
          ServiceRatesQuery.findById(newId)
        }).map { serviceRate =>
          jsonStatusOk(Json.obj( "serviceRate" -> serviceRate ))
        }

      case _ =>
        jsonErrorAccessDenied

    }
  }

  def update = apiWithParser( JsonModels.serviceRatesToJson ) { user => serviceRate =>
    db.run(
      ServicesQuery.hasAccess( user.id.getOrElse(0), serviceRate.serviceId, ObjectAccess.Write ).result.zip(
        ServiceRatesQuery.hasAccess( user.id.getOrElse(0), serviceRate.id.getOrElse(0), ObjectAccess.Write ).result.zip(
          ServiceRatesQuery.filter( _.id === serviceRate.id.getOrElse(0) ).result.headOption
        )
      )
    ).flatMap {
      case ( true, (true, Some( existingServiceRateData ) ) ) =>

        val updatedServiceRate = serviceRate.copy( isDeleted = false, serviceId = existingServiceRateData.serviceId )

        db.run( ServiceRatesQuery.update( updatedServiceRate.id.getOrElse(0), updatedServiceRate ) ).map { _ =>
          jsonStatusOk( Json.obj( "serviceRate" -> updatedServiceRate ) )
        }

      case _ =>
        jsonErrorAccessDenied
    }
  }

  def setActive( rateId:Int, isActive:Int ) = apiWithAuth { user => r =>
    db.run( ServiceRatesQuery.hasAccess( user.id.getOrElse(0), rateId, ObjectAccess.Write ).result ).flatMap {
      case true =>
        db.run( ServiceRatesQuery.filter( _.id === rateId ).map(_.isActive).update( isActive != 0 ) ).map { _ =>
          jsonStatusOk
        }

      case _ =>
        jsonErrorAccessDenied
    }
  }

  def delete( rateId:Int ) = apiWithAuth { user => r =>
    db.run( ServiceRatesQuery.hasAccess( user.id.getOrElse(0), rateId, ObjectAccess.Write ).result ).flatMap {
      case true =>
        db.run( ServiceRatesQuery.filter( _.id === rateId ).map(_.isDeleted).update( true ) ).map { _ =>
          jsonStatusOk
        }

      case _ =>
        jsonErrorAccessDenied
    }
  }


}
