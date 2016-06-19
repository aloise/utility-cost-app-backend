package controllers.api

import javax.inject.Inject

import models._
import models.base.{DBAccessProvider, ObjectAccess}
import models.helpers.{JsonJodaMoney, JsonModels}
import org.joda.money.{CurrencyUnit, Money}
import play.api.libs.json.Json
import slick.driver.H2Driver.api._
import slick.driver.JdbcProfile
import models.helpers.JsonJodaMoney._

import scala.concurrent.{ExecutionContext, Future}

/**
  * User: aloise
  * Date: 27.05.16
  * Time: 22:56
  */
class Places @Inject() ( implicit ec:ExecutionContext, db: DBAccessProvider ) extends ApiController(ec, db) {

  import models.helpers.JsonModels._

  def list = apiWithAuth { user:models.User => r =>
    db.run(PlacesQuery.forUser(user.id.getOrElse(0)).result).map { items =>
      jsonStatusOk(Json.obj("places"->items.map(_._2)))
    }
  }

  def globalStats = apiWithAuth { user: models.User => r =>

    val placesCountQuery =
      ( for {
        place <- PlacesQuery
        userPlaces <- UsersPlacesQuery
        if ( place.id === userPlaces.userId ) && ( userPlaces.userId === user.id ) && !place.isDeleted
      } yield place.id ).length

    val totalServicesQuery =
      ServicesQuery.userServices( user.id.getOrElse(0) ).length

    val totalPaidQuery =
      (
        for {
          bills <- BillsQuery
          place <- PlacesQuery
          userPlaces <- UsersPlacesQuery
          if
            ( place.id === userPlaces.userId ) &&
            ( userPlaces.userId === user.id ) &&
            ( bills.placeId === place.id ) &&
            !place.isDeleted &&
            !bills.isDeleted &&
            bills.paid.nonEmpty
        } yield ( bills.valueAmount, bills.valueCurrency )
      ).groupBy( _._2 ).map{ case ( currencyCode, q ) => ( currencyCode, q.map( _._1 ).sum ) }

    val totalUnpaidQuery =
      (
        for {
          bills <- BillsQuery
          place <- PlacesQuery
          userPlaces <- UsersPlacesQuery
          if
          ( place.id === userPlaces.userId ) &&
            ( userPlaces.userId === user.id ) &&
            ( bills.placeId === place.id ) &&
            !place.isDeleted &&
            !bills.isDeleted &&
            bills.paid.isEmpty
        } yield ( bills.valueAmount, bills.valueCurrency )
      ).groupBy( _._2 ).map{ case ( currencyCode, q ) => ( currencyCode, q.map( _._1 ).sum ) }

    val statsQuery = placesCountQuery.result zip totalServicesQuery.result zip totalPaidQuery.result zip totalUnpaidQuery.result

     db.run( statsQuery ).map { case ((( placesCount, totalServicesCount ), totalPaid ), totalUnpaid ) =>
       jsonStatusOk( Json.obj( "stats" -> Json.obj(
         "placesCount" -> placesCount,
         "totalServices" -> totalServicesCount,
         "totalPaid" -> totalPaid.map{ case ( cc, amt) => Money.of( CurrencyUnit.of(cc) , amt.getOrElse(BigDecimal(0)).bigDecimal ) },
         "totalDebt" -> totalUnpaid.map{ case ( cc, amt) => Money.of( CurrencyUnit.of(cc) , amt.getOrElse(BigDecimal(0)).bigDecimal ) }
       )))
     }

  }

  def get(placeId:Int) = apiWithAuth(PlacesQuery.hasReadAccess(placeId) _ ) { user => r =>
    db.run(PlacesQuery.findById(placeId)).map {
      case Some(item) => jsonStatusOk(Json.obj("place" -> item))
      case None => jsonErrorNotFound
    }
  }

  def create = apiWithParser(JsonModels.placeToJson) { user => place =>
    db.run(PlacesQuery.insert(place.copy(id=None, isDeleted = false)).flatMap { newId =>
      UsersPlacesQuery.insert(UsersPlace(user.id.getOrElse(-1), newId, UserRole.Admin))
      PlacesQuery.findById(newId)
    }).map { place =>
      jsonStatusOk(Json.obj("place" -> Json.toJson(place)))
    }
  }

  def update = apiWithParserModel(JsonModels.placeToJson) ( (p, uId) => PlacesQuery.hasWriteAccess(p.id.getOrElse(0), uId) ) { user => place =>

    place.id match {
      case Some(id) =>
        db.run(PlacesQuery.findById(id))
            .flatMap {
              case Some(existingPlace) =>
                db.run(PlacesQuery.update(id, place.copy(isDeleted = false))).map { _ =>
                  jsonStatusOk(Json.obj("place" -> place))
                }
              case None =>
                jsonErrorNotFoundFuture
            }
      case None => recoverJsonErrorsFuture("no_id")
    }


  }

  def delete(placeId:Int) = apiWithAuth(PlacesQuery.hasWriteAccess(placeId) _ ) { user => r =>
    db.run(PlacesQuery.deleteById(placeId)).map { updatedPlace =>
      jsonStatusOk
    }
  }
}
