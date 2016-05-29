package controllers.api

import javax.inject.Inject

import models.{PlacesQuery, UserRole, UsersPlace, UsersPlacesQuery}
import models.base.DBAccessProvider
import models.helpers.ModelToJsonHelper
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
      jsonStatusOk
    }
  }

  def create = apiWithParser(ModelToJsonHelper.placesToJson) { user => place =>
    db.run(PlacesQuery.insert(place).flatMap { newId =>
      PlacesQuery.findById(newId)
    }).map { place =>
      jsonStatusOk(Json.obj("place" -> Json.toJson(place)))
    }
  }

  def update = apiWithParser(ModelToJsonHelper.placesToJson) { user => place =>

    place.id match {
      case Some(id) =>
        db.run(
          UsersPlacesQuery.findUserPlace(user.id.getOrElse(-1), id, UserRole.Admin)
            .flatMap { userPlace =>
              PlacesQuery.update(id, place).flatMap(PlacesQuery.findById).map { updatedPlace =>
                jsonStatusOk(Json.obj("place" -> Json.toJson(updatedPlace)))
              }
            })
      case None => recoverJsonErrorsFuture("no_id")
    }


  }

}
