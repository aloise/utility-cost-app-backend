package models.helpers

import models._
import play.api.libs.json.Json

/**
  * Created by aeon on 15/03/16.
  */
object ModelToJsonHelper {

  implicit val userToJson = Json.format[User]

  implicit val placesToJson = Json.format[Place]

}
