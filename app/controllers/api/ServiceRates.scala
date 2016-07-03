package controllers.api

import java.time.LocalDateTime

import models._
import models.base.{DBAccessProvider, ObjectAccess => O}
import models.helpers.JsonModels._
import slick.driver.H2Driver.api._
import models.helpers.SlickColumnExtensions._
import play.api.libs.json.Json
import javax.inject.Inject

import models.helpers.{JsonJodaMoney, JsonModels}

import scala.concurrent.ExecutionContext

/**
  * User: aloise
  * Date: 30.05.16
  * Time: 22:27
  */
class ServiceRates @Inject()(implicit ec:ExecutionContext, db: DBAccessProvider ) extends ApiController(ec, db) {


  def listForService( serviceId:Int, includeInactive:Int = 0 ) = apiWithAuth( ServicesQuery.hasReadAccess( serviceId ) _ ) { user => r =>

    db.run(ServiceRatesQuery.forService(serviceId, includeInactive != 0).result).map { serviceRates =>
      jsonStatusOk(Json.obj("serviceRates" -> serviceRates))
    }
  }

  def get( rateId:Int ) = apiWithAuth( ServiceRatesQuery.hasAccess(O.Read)(rateId) _ ) { user => r =>
      db.run( ServiceRatesQuery.filter( _.id === rateId ).result.headOption ).map { serviceRate =>
        jsonStatusOk(Json.obj("serviceRate" -> serviceRate ))
      }
  }

  def create = apiWithParserModel( JsonModels.serviceRatesToJson )( (sr,uId) => ServicesQuery.hasWriteAccess( sr.serviceId )(uId) ){ user => serviceRate =>

    val serviceRateData = serviceRate.copy( id = None, isDeleted = false )

    db.run(ServiceRatesQuery.insert(serviceRateData)).flatMap { newId =>
      db.run( ServiceRatesQuery.findById(newId) )
    }.map { serviceRate =>
      jsonStatusOk(Json.obj( "serviceRate" -> serviceRate ))
    }
  }

  def update = apiWithParser( JsonModels.serviceRatesToJson ){ user:models.User => serviceRate =>
    db.run(
      ServicesQuery.hasWriteAccess( serviceRate.serviceId , user.id.getOrElse(0) ).result.zip(
        ServiceRatesQuery.hasWriteAccess( serviceRate.id.getOrElse(0) , user.id.getOrElse(0) ).result.zip(
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

  def setActive( rateId:Int, isActive:Int ) = apiWithAuth( ServiceRatesQuery.hasWriteAccess(rateId) _ ) { user => r =>
    db.run( ServiceRatesQuery.filter( _.id === rateId ).map(_.isActive).update( isActive != 0 ) ).map { _ =>
      jsonStatusOk
    }
  }

  def delete( rateId:Int ) = apiWithAuth( ServiceRatesQuery.hasWriteAccess(rateId) _ ) { user => r =>
      db.run( ServiceRatesQuery.filter( _.id === rateId ).map(_.isDeleted).update( true ) ).map { _ =>
        jsonStatusOk
      }
  }

  def updateActiveForService( serviceId:Int ) = apiWithParserHasAccess( JsonModels.serviceRatesToJson )( ServicesQuery.hasReadAccess( serviceId ) _ ) {
    user: models.User => serviceRate =>
      val setInactiveQuery = ServiceRatesQuery.filter{ serviceRate =>
        serviceRate.serviceId === serviceId && serviceRate.isActive
      }.map( sr => ( sr.isActive, sr.inactiveFromDate ) ).update( ( false, Some( LocalDateTime.now() ) ) )

      val createNewQuery = ServiceRatesQuery.insert(
        serviceRate.copy(
          id = None,
          serviceId = serviceId,
          isActive = true,
          activeFromDate = LocalDateTime.now(),
          isDeleted = false
        )
      )

      val updateResultQuery =
        for {
          updatedInactive <- setInactiveQuery
          id <- createNewQuery
          newServiceRate <- models.ServiceRatesQuery.findById( id )
        } yield newServiceRate

      db.run( updateResultQuery.transactionally ).map{ newServiceRate =>
        jsonStatusOk( Json.obj(  "serviceRate" -> newServiceRate ) )
      }


  }


}
