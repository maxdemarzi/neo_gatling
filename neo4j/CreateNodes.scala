//package request-bodies

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import akka.util.duration._
import bootstrap._


class CreateNodes extends Simulation {
  val httpConf = httpConfig
    .baseURL("http://localhost:7474")
    .acceptHeader("application/json")

  val createNode = """{"query": "start n=node(0) foreach(id in range(1,1000) : create (n))"}"""

  val scn = scenario("Create Nodes")
    .repeat(10) {
    exec(
      http("create node")
        .post("/db/data/cypher")
        .body(createNode)
        .asJSON
        .check(status.is(200)))
      .pause(0 milliseconds, 1 milliseconds)
  }


  setUp(
    scn.users(10).ramp(10).protocolConfig(httpConf)
  )
}

