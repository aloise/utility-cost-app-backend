package controllers.api

import javax.inject.Inject

import models.PlacesQuery
import models.base.DBAccessProvider
import slick.driver.H2Driver.api._
import slick.driver.JdbcProfile
import scala.concurrent.ExecutionContext

/**
  * User: aloise
  * Date: 27.05.16
  * Time: 22:56
  */
class Places @Inject() ( implicit ec:ExecutionContext, db: DBAccessProvider, users: models.Users ) extends ApiController(ec, users) {

  def list = apiWithAuth { user => r =>
    db.run{
      PlacesQuery().forUser(user.id.getOrElse(0)).result
    }.map{ items =>
      jsonStatusOk
    }

  }

}
