import models.rate_data._
import org.joda.money.{CurrencyUnit, Money}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.PlaySpec

/**
  * User: aloise
  * Date: 01.06.16
  * Time: 22:42
  */
class RateCalculationSpec extends PlaySpec {

  "Rate data" must {

    "return the correct manual price" in {
      val rate = new ManualPriceRateData()

      rate.calculatePricePerMonth(10, 20).getAmount.intValue() mustBe 0
    }

    "return the correct fixed price" in {
      val rate = new FixedPriceRateData( Money.of( CurrencyUnit.USD, 200 ) )

      rate.calculatePricePerMonth( 10, 20 ).getAmount.intValue() mustBe 200
      rate.calculatePricePerMonth( 10, 20 ).getCurrencyUnit mustBe CurrencyUnit.USD

    }

    "return the correct multi-rate price" in {

      val curr = CurrencyUnit.EUR

      val rateData = Seq(
        (BigDecimal(50),Money.of( curr, 1 )),
        (BigDecimal(250),Money.of( curr, 2 )),
        (BigDecimal(500),Money.of( curr, 3 ) )
      )

      val rate = new MultiRateData( rateData , Money.of( curr, 5  ) )
      val price = rate.calculatePricePerMonth( 100, 700 )

      price.getAmount.intValue() mustBe ( 1*50 + 2*200 + 3*250 + 5*100 )
      price.getCurrencyUnit mustBe curr

    }

  }

}
