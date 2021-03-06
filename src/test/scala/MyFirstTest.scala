import io.gatling.core.Predef._
import io.gatling.http.Predef._

class MyFirstTest extends Simulation {


  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")
    .proxy(Proxy("localhost", 8888))


  // 2: Scenario Definition (steps in user journey)
  val scn = scenario("My first test")
    .exec(http("Get All Games")
    .get("videogames"))

  // 3: Load Scenario

  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(httpConf)

}
