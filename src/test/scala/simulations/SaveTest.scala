package simulations

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

class SaveTest extends Simulation{

    val httpConf = http.baseUrl("http://localhost:8080/app/")
        .header("Accept", "application/json")

    def saveAndGetGameId(): ChainBuilder = {
        exec(
            http("Get a game ID")
                .get("videogames")
                .check(jsonPath("$[0].id").saveAs("gameId"))
                .check(status.is(200))
        )
        .exec(
            http("use saved ID")
                .get("videogames/${gameId}")
                .check(jsonPath("$.name").is("Resident Evil 4"))
        )
    }

    val scn = scenario("Please let this work")
        .exec(saveAndGetGameId())
    setUp(
        scn.inject(
            atOnceUsers(1)
        ).protocols(httpConf)
    )
}
//package simulations
//
//import io.gatling.core.Predef._
//import io.gatling.http.Predef._
//
//class SaveTest extends Simulation {
//
//    val httpConf = http.baseUrl("http://localhost:8080/app/")
//        .header("Accept", "application/json")
//
//    def bruhMoment() = {
//        // First call - check the name of the game
//        exec(http("Get specific game")
//            .get("videogames/1")
//            .check(jsonPath("$.name").is("Resident Evil 4")))
//
//        // Second call - extract the ID of a game and save it to a variable called gameId
//        .exec(http("Get all video games")
//            .get("videogames")
//            .check(jsonPath("$[1].id").saveAs("gameId")))
//
//        // Third call - use the gameId variable saved from the above call
//        .exec(http("Get specific game")
//            .get("videogames/${gameId}")
//            .check(jsonPath("$.name").is("Gran Turismo 3")))
//    }
//
//    val scn = scenario("Check JSON Path")
//        .exec(bruhMoment())
//
//
//
//        setUp(
//            scn.inject(atOnceUsers(1))
//        ).protocols(httpConf)
//
//}