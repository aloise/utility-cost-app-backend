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

  def list = apiWithAuth { user:models.User => r =>
    db.run(PlacesQuery.forUser(user.id.getOrElse(0)).result).map { items =>
      jsonStatusOk(Json.obj("places"->items.map(_._2)))
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
