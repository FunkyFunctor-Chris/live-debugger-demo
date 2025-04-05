package example

import com.thedeanda.lorem.LoremIpsum
import example.WithdrawalBodyProvider.FEEDER_BODY_KEY
import io.circe.generic.auto._
import io.circe.syntax._

import scala.util.Random

trait WithdrawalBodyProvider {
  val feeder: Iterator[Map[String, String]] = Iterator.continually(
    Map(
      FEEDER_BODY_KEY -> body()
    )
  )

  def body(): String
}

object WithdrawalBodyProvider {
  val FEEDER_BODY_KEY = "body"

  object NormalUserProvider extends WithdrawalBodyProvider {
    val MAX_NB_USERS = 100

    val USERS: Seq[String] =
      List.fill(MAX_NB_USERS)(LoremIpsum.getInstance().getName)

    override def body(): String = WithdrawalRequest(
      USERS(Random.nextInt(MAX_NB_USERS)),
      Random.nextInt(900) + 100
    ).asJson.noSpaces
  }

  object HackerProvider extends WithdrawalBodyProvider {
    override val body: String =
      WithdrawalRequest("Nastya Coeur", Int.MinValue).asJson.noSpaces
  }

  case class WithdrawalRequest(accountId: String, amount: Int)
}
