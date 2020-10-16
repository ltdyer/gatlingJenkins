package finalSimulation
import java.time.LocalDate
import java.time.format.DateTimeFormatter


import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.util.Random

class VideoGameFullTestTemplate extends Simulation {

    /*** HTTP Conf ***/
    val httpConf = http.baseUrl("http://localhost:8080/app/")
        .header("Accept", "application/json")


    /*** CSV Feeder ***/
    val feeder = csv("data/gameCsvFile.csv").circular


    /*** Easy Iterator Feeder ***/
    val ids = (1 to 10).iterator
    val customFeederEasy = Iterator.continually(Map("gameId" -> ids.next()))


    /*** Hard but I guess cooler Feeder ***/
    //ids, random, date, string generator
    val newids = (21 to 30).iterator
    val rand = new Random()
    val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val now = LocalDate.now()
    def randomString(length: Int) = {
        rand.alphanumeric.filter(_.isLetter).take(length).mkString
    }
    def getRandomDate(startDate: LocalDate, random: Random): String = {
        startDate.minusDays(random.nextInt(30)).format(pattern)
    }
    val customFeederHard = Iterator.continually(Map(
        "gameId" -> newids.next(),
        "name" -> ("Game+" + randomString(5)),
        "releaseDate" -> getRandomDate(now, rand),
        "reviewScore" -> rand.nextInt(100),
        "category" -> ("Category+" + randomString(4)),
        "rating" -> ("Rating+" + randomString(6))
    ))

    /*** Runtime Parameters ***/
    private def getProperty(propertyName: String, defaultValue: String) = {
        Option(System.getenv(propertyName))
            .orElse(Option(System.getProperty(propertyName)))
            .getOrElse(defaultValue)
    }
    val userCount: Int = getProperty("USERS", "5").toInt
    val rampDuration: Int = getProperty("RAMP_DURATION", "10").toInt
    val testDuration: Int = getProperty("DURATION", "60").toInt
    before {
        println(s"Testing with ${userCount} users")
        println(s"Ramp duration is ${rampDuration} seconds")
        println(s"Test duration is ${testDuration} seconds")
    }



    /** HTTP CALLS */
    def getAllVideoGames() = {
        exec(
            http("Get All Video Games")
                .get("videogames")
                .check(status.is(200))

        )
    }

    def getSpecificVideoGame() = {
        exec(
            http("Get specific video game")
                .get("videogames/1")
                .check(jsonPath("$.name").is("Resident Evil 4"))
        )
    }

    def getAndSaveGameID() = {
        exec(
            http("Get and save Game Id")
                .get("videogames")
                .check(jsonPath("$[0].id").saveAs("gameId"))
        )
            .exec(
                http("Use saved GameId")
                    .get("videogames/${gameId}")
                    .check(jsonPath("$.name").is("Resident Evil 4"))
            )
    }

    def getSpecificVideoGameViaCsv() = {
        repeat(10) {
            feed(feeder)
                .exec(http("Get specific video game with csv")
                    .get("videogames/${gameId}")
                    .check(jsonPath("$.name").is("${gameName}"))
                    .check(status.is(200)))
        }

    }

    def getSpecificVideoGameViaCustomEasy() = {
        repeat(10) {
            feed(customFeederEasy)
                .exec(http("Get specific video game with easy custom feeder")
                    .get("videogames/${gameId}")
                    .check(status.is(200)))
        }
    }

    def createNewVideoGames() = {
        repeat(10) {
            feed(customFeederHard)
                .exec(http("Create new set of games")
                    .post("videogames")
                    .body(ElFileBody("bodies/NewGameTemplate.json")).asJson
                    .check(status.is(200)))
        }
    }

    def deleteVideoGame() = {
        exec(
            http("Delete video game")
                .delete("videogames/30")
                .check(jsonPath("$.gameId").notExists)
        )
    }

    val scn1 = scenario("Get All vidya")
        .exec(getAllVideoGames())
    val scn2 = scenario("Get Specific vidya")
        .exec(getSpecificVideoGame())
    val scn3 = scenario("Get with CSV")
        .exec(getSpecificVideoGameViaCsv())
    val scn4 = scenario("Get with custom feeder")
        .exec(getSpecificVideoGameViaCustomEasy())
    val scn5 = scenario("Create new vidya")
        .exec(createNewVideoGames())
    val scn6 = scenario("Delete vidya")
        .exec(deleteVideoGame())
    val scn7 = scenario("Calls a bunch of APIs forever")
        .forever {
            exec(getAllVideoGames())
                .pause(5)
                .exec(getSpecificVideoGame())
                .pause(5)
                .exec(getAllVideoGames())
        }
    val scn8 = scenario("Calls a bunch of APIs with various amounts of users")
        .exec(getAllVideoGames())
        .pause(5)
        .exec(getSpecificVideoGame())
        .pause(5)
        .exec(getAllVideoGames())
    val scn9 = scenario("Get, save, and check game ID")
        .exec(getAndSaveGameID())

    //    setUp(
    //        scn7.inject(
    //            nothingFor(5),
    //            rampUsers(userCount) during (rampDuration)
    //        ).protocols(httpConf)
    //    ).maxDuration(testDuration)
    setUp(
        scn9.inject(
            nothingFor(5),
            atOnceUsers(1),
            rampUsers(userCount) during (rampDuration)
        ).protocols(httpConf)
    )







}
