package models.helpers

import play.api.libs.json.Json
import models._
import SlickColumnExtensions._
import JsonJodaMoney._


/**
  * User: aloise
  * Date: 29.05.16
  * Time: 14:58
  */
object JsonModels {

  implicit val billToJson = Json.format[Bill]
  implicit val placeToJson = Json.format[Place]
  implicit val placesServiceToJson = Json.format[PlacesService]
  implicit val serviceRatesToJson = Json.format[ServiceRate]
  implicit val serviceToJson = Json.format[Service]
  implicit val userToJson = Json.format[User]
  implicit val userRoleToJson = EnumUtils.enumFormat(models.UserRole)
  implicit val userPlaceToJson = Json.format[UsersPlace]

}
