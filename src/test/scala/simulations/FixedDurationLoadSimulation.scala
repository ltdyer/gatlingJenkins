package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class FixedDurationLoadSimulation extends Simulation{

    val httpConf = http.baseUrl("http://localhost:8080/app/")
        .header("Accept", "application/json")

    def getAllVideoGames() = {
        exec(
            http("get all video games")
                .get("videogames")
                .check(status.is(200))
        )
    }

    def getSpecificGame() = {
        exec(
            http("get Specific Game")
                .get("videogames/2")
                .check(status.is(200))
        )
    }

    val scn = scenario("Fixed Duration Load Sim")
        .forever() {
            exec(getAllVideoGames())
            .pause(5)
            .exec(getSpecificGame())
            .pause(5)
            .exec(getAllVideoGames())
        }


    setUp(
        scn.inject(
            nothingFor(5 seconds),
            //constantUsersPerSec(10) during (10 seconds)
            //rampUsersPerSec(1) to (5) during (20 seconds)
            atOnceUsers(10),
            rampUsers(50) during (30 seconds)
        ).protocols(httpConf.inferHtmlResources())
    ).maxDuration(1 minute)
}
