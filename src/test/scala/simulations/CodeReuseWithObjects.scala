package simulations

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

class CodeReuseWithObjects extends Simulation {


  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")




  def getAllVideoGames(): ChainBuilder = {

    repeat(3) {
      exec(http("Get all video games - 1st call")
        .get("videogames")
        .check(status.is(200)))
    }
  }


  def getSpecificGame(): ChainBuilder ={
    repeat(5) {
      exec(http("Get a specific game")

        .get("videogames/1")
        .check(status.in(200 to 210)))
    }
  }

  val scn = scenario("Code reuse")

    .exec(getAllVideoGames())
    .pause(5)
    .exec(getSpecificGame())
    .pause(5)
    .exec(getAllVideoGames())


  setUp(scn.inject(atOnceUsers(1))).protocols(httpConf)

}
