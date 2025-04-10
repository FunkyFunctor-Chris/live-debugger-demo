package example

import example.WithdrawalBodyProvider.FEEDER_BODY_KEY
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt
import scala.util.Try

class BasicSimulation extends Simulation {
  // Reference: https://docs.gatling.io/guides/passing-parameters/
  val TEST_DURATION: Int = Option(System.getenv("TEST_DURATION"))
    .flatMap(str => Try(str.toInt).toOption)
    .getOrElse(10)
  val TARGET_SERVER: String = Option(System.getenv("TARGET_SERVER")).getOrElse("demo.funky-functor.com:8080")
  val LR_QUERIES_DURATION: Int = Option(System.getenv("LR_QUERIES_DURATION"))
    .flatMap(str => Try(str.toInt).toOption)
    .getOrElse(10)

  java.lang.System.err.println(s"Running test for $TEST_DURATION minutes against server '$TARGET_SERVER' - Long running queries are $LR_QUERIES_DURATION seconds long")

  // Define HTTP configuration
  // Reference: https://docs.gatling.io/reference/script/protocols/http/protocol/
  private val httpProtocol = http
    .baseUrl(s"http://$TARGET_SERVER")
    .acceptHeader("application/json")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36")

  // Define scenario
  // Reference: https://docs.gatling.io/reference/script/core/scenario/
  private val scenario1 = scenario("Scenario 1 - Regular users")
    .feed(WithdrawalBodyProvider.NormalUserProvider.feeder)
    .exec(
      http("Withdrawal")
        .post("/withdraw")
        .body(StringBody(s"#{$FEEDER_BODY_KEY}"))
        .asJson
    )

  private val scenario2 = scenario("Scenario 2 - Hacker")
    .feed(WithdrawalBodyProvider.HackerProvider.feeder)
    .exec(
      http("Withdrawal")
        .post("/withdraw")
        .body(StringBody(s"#{$FEEDER_BODY_KEY}"))
        .asJson,
      http("Reset accounts")
        .get("/reset")
    )

  private val scenario3 = scenario("Scenario 3 - 4XX errors - long running queries")
    .feed(WithdrawalBodyProvider.NormalUserProvider.feeder)
    .exec(
      http("Long Running pages")
        .get(s"/long-running/$LR_QUERIES_DURATION")
    )

  // Define assertions
  // Reference: https://docs.gatling.io/reference/script/core/assertions/
  private val assertion = global.failedRequests.count.lt(1)

  // Define injection profile and execute the test
  // Reference: https://docs.gatling.io/reference/script/core/injection/
  setUp(
    scenario3.inject(constantUsersPerSec(5) during (30 seconds)),
    scenario1.inject(constantUsersPerSec(5) during (TEST_DURATION minutes)),
    scenario2.inject(constantUsersPerSec(2 / 60f) during (TEST_DURATION minutes)),
  ).assertions(assertion).protocols(httpProtocol)
}
