package models.rate_data

import julienrf.json.derived
import models.rate_data.RateDataContainer.RateData
import play.api.libs.json.OFormat

/**
  * User: aloise
  * Date: 02.06.16
  * Time: 13:12
  */
class RateDataJson {


    implicit val rateDataFormat: OFormat[RateData] = derived.oformat[RateData]

}
