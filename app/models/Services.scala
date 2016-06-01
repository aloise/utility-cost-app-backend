package models

import java.time.LocalDateTime
import javax.inject.Inject

import models.base.ObjectAccess.Access
import models.base._
import models.helpers._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.Codecs
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsObject, JsValue}
import slick.driver.H2Driver.api._
import models.helpers.SlickColumnExtensions._



/**
  * User: aloise
  * Date: 23.05.16
  * Time: 22:18
  */
case class Service(
  override val id:Option[Int],
  title:String,
  area:String,
  description:String,
  createdByUserId:Int,
  override val isDeleted:Boolean = false
) extends IndexedRow

class ServicesTable(tag:Tag) extends IndexedTable[Service](tag, "services") {

  def title = column[String]("title")
  def area = column[String]("area")
  def description = column[String]("description")
  def createdByUserId = column[Int]("created_by_user_id")

  def * = (id.?, title,area, description, createdByUserId, isDeleted) <> ( Service.tupled, Service.unapply )

}

object ServicesQuery extends IndexedTableQuery[Service, ServicesTable]( tag => new ServicesTable(tag) ) with UserHasAccess[Service,ServicesTable] {

  def listByPlace( userId: Int, placeId:Int ) = {
    for {
      service <- ServicesQuery
      servicePlace <- PlacesServicesQuery
      place <- PlacesQuery
      userPlace <- UsersPlacesQuery
      if
        !service.isDeleted &&
        !place.isDeleted &&
        ( service.id === servicePlace.serviceId ) &&
        ( servicePlace.placeId === place.id ) &&
        ( place.id === userPlace.placeId ) &&
        ( userPlace.userId === userId ) &&
        ( place.id === placeId )

    } yield service
  }

  override def hasAccess(serviceId: Rep[Int])(userId: Rep[Int], access: Access): Rep[Boolean] =
    access match {
      case ObjectAccess.Write =>
        ServicesQuery.
          filter(s => (s.createdByUserId === userId) && (s.id === serviceId) && !s.isDeleted ).
          exists

      case ObjectAccess.Read =>
        this.hasAccess(serviceId)( userId, ObjectAccess.Write ) ||
          (
            for {
              service <- ServicesQuery
              servicePlace <- PlacesServicesQuery
              place <- PlacesQuery
              userPlace <- UsersPlacesQuery
              if
              !service.isDeleted &&
                !place.isDeleted &&
                ( service.id === serviceId ) &&
                ( service.id === servicePlace.serviceId ) &&
                ( servicePlace.placeId === place.id ) &&
                ( place.id === userPlace.placeId ) &&
                ( userPlace.userId === userId )

            } yield service.id
          ).exists
    }

}

