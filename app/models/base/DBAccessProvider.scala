package models.base

import javax.inject.Inject

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.dbio.{DBIOAction, NoStream}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * User: aloise
  * Date: 27.05.16
  * Time: 23:41
  */
class DBAccessProvider @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]{

  def run[R](action: DBIOAction[R, NoStream, Nothing]): Future[R] =
    db.run[R]( action )

}
