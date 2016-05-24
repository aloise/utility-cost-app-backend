package helpers

import org.keyczar.Crypter
/**
  * Created by aeon on 24/05/16.
  */

object CryptoHelper {

  val crypter = new Crypter("keys")

  def encryptAES(s:String) = {
    crypter.encrypt(s)
  }

  def decryptAES(s:String) = {
    crypter.decrypt(s)
  }

}
