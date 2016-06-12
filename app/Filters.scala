import javax.inject.Inject

import play.api.http._
import play.api.mvc.EssentialFilter
import play.filters.cors.CORSFilter
/**
  * User: aloise
  * Date: 12.06.16
  * Time: 17:05
  */


class Filters @Inject() ( filter:CORSFilter ) extends HttpFilters{
  override def filters: Seq[EssentialFilter] = Seq( filter )
}