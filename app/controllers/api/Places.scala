package controllers.api

import javax.inject.Inject

import models.{PlacesQuery, UserRole, UsersPlace, UsersPlacesQuery}
import models.base.{DBAccessProvider, ObjectAccess}
import models.helpers.JsonModels
import play.api.libs.json.Json
import slick.driver.H2Driver.api._
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext

/**
  * User: aloise
  * Date: 27.05.16
  * Time: 22:56
  */
class Places @Inject() ( implicit ec:ExecutionContext, db: DBAccessProvider ) extends ApiController(ec, db) {

  import models.helpers.JsonModels._

  def list = apiWithAuth { user => r =>
    db.run(PlacesQuery.forUser(user.id.getOrElse(0)).result).map { items =>
      jsonStatusOk(Json.obj("places"->items.map(_._2)))
    }
  }

  def get(placeId:Int) = apiWithAuth { user => r =>
    db.run(PlacesQuery.findPlaceWithAccess(placeId, user.id.getOrElse(0), ObjectAccess.Read)).map {
      case Some(item) => jsonStatusOk(Json.obj("place" -> item))
      case None => recoverJsonErrors("place_not_found")
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

  def update = apiWithParser(JsonModels.placeToJson) { user => place =>

    place.id match {
      case Some(id) =>
        db.run(UsersPlacesQuery.findUserPlace(user.id.getOrElse(-1), id, UserRole.Admin))
            .flatMap {
              case Some(userPlace) =>
                db.run(PlacesQuery.update(id, place).flatMap(PlacesQuery.findById)).map { updatedPlace =>
                  jsonStatusOk(Json.obj("place" -> updatedPlace))
                }
              case None =>
                recoverJsonErrorsFuture("user_place_not_found")
            }
      case None => recoverJsonErrorsFuture("no_id")
    }


  }

  def delete(placeId:Int) = apiWithAuth { user => r =>
    db.run(UsersPlacesQuery.findUserPlace(user.id.getOrElse(-1), placeId, UserRole.Admin))
      .flatMap {
        case Some(userPlace) =>
          db.run(PlacesQuery.deleteById(userPlace.placeId)).map { updatedPlace =>
            jsonStatusOk
          }
        case None =>
          recoverJsonErrorsFuture("user_place_not_found")
      }

  }

}
