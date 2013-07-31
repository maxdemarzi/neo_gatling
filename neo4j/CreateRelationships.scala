//package request-bodies

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import akka.util.duration._
import bootstrap._
import util.parsing.json.JSONObject


class CreateRelationships extends Simulation {
  val httpConf = httpConfig
    .baseURL("http://localhost:7474")
    .acceptHeader("application/json")
    .requestInfoExtractor(request => {
      println(request.getStringData)
      Nil
    })


  val rnd = new scala.util.Random
  val chooseRandomNodes = exec((session) => {
    session.setAttribute("params", JSONObject(Map("id1" -> rnd.nextInt(100000),
                                                  "id2" -> rnd.nextInt(100000))).toString())
  })

  val createRelationship = """START node1=node({id1}), node2=node({id2}) CREATE UNIQUE node1-[:KNOWS]->node2"""
  val cypherQuery = """{"query": "%s", "params": %s }""".format(createRelationship, "${params}")


  val scn = scenario("Create Relationships")
    .during(30) {
    exec(chooseRandomNodes)
      .exec(
        http("create relationships")
          .post("/db/data/cypher")
          .header("X-Stream", "true")
          .body(cypherQuery)
          .asJSON
          .check(status.is(200)))
      .pause(0 milliseconds, 5 milliseconds)
  }

  setUp(
    scn.users(100).ramp(10).protocolConfig(httpConf)
  )
}

