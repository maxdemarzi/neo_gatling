//package request-bodies

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import akka.util.duration._
import bootstrap._


class GetRoot extends Simulation {
  val httpConf = httpConfig
    .baseURL("http://localhost:7474")
    .acceptHeader("application/json")

  val scn = scenario("Get Root")
   .during(10) {
     exec(
       http("get root node")
         .get("/db/data/node/0")
         .check(status.is(200)))
     .pause(0 milliseconds, 5 milliseconds)
   }

  setUp(
    scn.users(100).protocolConfig(httpConf)
  )
}

