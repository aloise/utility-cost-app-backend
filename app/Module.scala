

import com.google.inject.AbstractModule
import play.api._
/**
  * User: aloise
  * Date: 29.05.16
  * Time: 14:07
  */
class Module(environment: Environment, configuration: Configuration) extends AbstractModule {
  override def configure(): Unit = {

    Logger.debug("Utility Billing Code Backend Started")
  }
}
