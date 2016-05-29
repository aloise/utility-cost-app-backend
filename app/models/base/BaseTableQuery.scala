package models.base

import slick.lifted.TableQuery
import slick.lifted._

/**
  * User: aloise
  * Date: 28.05.16
  * Time: 18:34
  */
abstract class BaseTableQuery[R,T <: BaseTable[R] ]( builder: Tag => T ) extends TableQuery[T](builder){


}
