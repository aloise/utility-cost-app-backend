package models.helpers

import javax.inject.Inject

import models._
import play.api.db.slick.DatabaseConfigProvider
import slick.lifted.TableQuery

/**
  * User: aloise
  * Date: 24.05.16
  * Time: 18:05
  */
class DBDataProvider @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {

  lazy val bills = TableQuery[Bills]
  lazy val places = TableQuery[Places]
  lazy val placesServices = TableQuery[PlacesServices]
  lazy val serviceRates = TableQuery[ServiceRates]
  lazy val services = TableQuery[Services]
  lazy val users = TableQuery[Users]
  lazy val usersPlaces = TableQuery[UsersPlaces]

}
