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


  protected def recoverJsonErrorsFuture( errors:JsError ):Future[Result] =
    Future.successful( BadRequest(Json.obj(
      "status" ->"error",
      "message" -> JsError.toJson(errors)
    )) )

  protected def recoverJsonErrorsFuture( error:String, description: String = null ):Future[Result] =
    Future.successful( BadRequest(Json.obj("status" ->"error", "message" -> error, "description" -> description )))

  protected def recoverJsonExceptionFuture( error:Throwable ):Future[Result] =
    Future.successful( BadRequest( Json.toJson( error )) )

  protected def recoverJsonException( error:Throwable ):Result =
    BadRequest( Json.toJson(error) )

  protected def recoverJsonErrors( errors:JsError ):Result =
    recoverJsonErrors( errors, Json.obj())


  protected def recoverJsonErrors( errors:JsError, additionalErrorObj : JsObject ):Result =
    BadRequest(Json.obj(
      "status" -> "error",
      "message" -> JsError.toFlatJson(errors)
    ) ++ additionalErrorObj )

  protected def recoverJsonErrors( error:String, description: String = null ):Result =
    recoverJsonErrors(error, Json.obj( "description" -> description ))

  protected def recoverJsonErrors( error:String, additionalErrorObj : JsObject ):Result =
    BadRequest(Json.obj( "status" -> "error", "message" -> error ) ++ additionalErrorObj )


  protected def jsonStatusOk:Result = jsonStatusOk(Json.obj())

  protected def jsonStatusAccepted:Result = jsonStatusAccepted(Json.obj())

  protected def jsonStatusOk( additionalData : JsObject ) =
    Ok(Json.obj("status" -> "ok") ++ additionalData)

  protected def jsonStatusAccepted( additionalData : JsObject ) =
    Accepted(Json.obj("status" -> "ok") ++ additionalData)

  protected def jsonStatusOkFuture( additionalData : JsObject ) =
    Future.successful( Ok(Json.obj("status" -> "ok") ++ additionalData) )
  
}