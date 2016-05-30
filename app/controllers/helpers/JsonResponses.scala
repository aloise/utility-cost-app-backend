package controllers.helpers

import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent._

trait JsonResponses {
  
  class ControllerErrorException extends Throwable

  implicit val jsonThrowableWriter = new Writes[Throwable]{
	  def writes(ex: Throwable) = Json.obj(
	    "message" -> ex.getMessage,
	    "description" -> ex.getClass.getName.replace("$", "."),
	    "status" -> "error"
	  )      
  }


  def recoverJsonErrorsFuture( errors:JsError ):Future[Result] =
    Future.successful( BadRequest(Json.obj(
      "status" ->"error",
      "message" -> JsError.toJson(errors)
    )) )

  def jsonErrorAccessDenied = recoverJsonErrorsFuture("access_denied")

  def recoverJsonErrorsFuture( error:String, description: String = null ):Future[Result] =
    Future.successful( BadRequest(Json.obj("status" ->"error", "message" -> error, "description" -> description )))

  def recoverJsonExceptionFuture( error:Throwable ):Future[Result] =
    Future.successful( BadRequest( Json.toJson( error )) )

  def recoverJsonException( error:Throwable ):Result =
    BadRequest( Json.toJson(error) )

  def recoverJsonErrors( errors:JsError ):Result =
    recoverJsonErrors( errors, Json.obj())


  def recoverJsonErrors( errors:JsError, additionalErrorObj : JsObject ):Result =
    BadRequest(Json.obj(
      "status" -> "error",
      "message" -> JsError.toJson(errors )
    ) ++ additionalErrorObj )

  def recoverJsonErrors( error:String, description: String = null ):Result =
    recoverJsonErrors(error, Json.obj( "description" -> description ))

  def recoverJsonErrors( error:String, additionalErrorObj : JsObject ):Result =
    BadRequest(Json.obj( "status" -> "error", "message" -> error ) ++ additionalErrorObj )


  def jsonStatusOk:Result = jsonStatusOk(Json.obj())

  def jsonStatusAccepted:Result = jsonStatusAccepted(Json.obj())

  def jsonStatusOk( additionalData : JsObject ) =
    Ok(Json.obj("status" -> "ok") ++ additionalData)

  def jsonStatusAccepted( additionalData : JsObject ) =
    Accepted(Json.obj("status" -> "ok") ++ additionalData)

  def jsonStatusOkFuture( additionalData : JsObject = Json.obj() ) =
    Future.successful( Ok(Json.obj("status" -> "ok") ++ additionalData) )
  
}