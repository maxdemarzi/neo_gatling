//package request-bodies

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import akka.util.duration._
import bootstrap._
import util.parsing.json.{JSON, JSONArray}


class CreateRelationships extends Simulation {
  val httpConf = httpConfig
    .baseURL("http://localhost:7474")
    .acceptHeader("application/json")
	.acceptEncodingHeader("gzip, deflate")
    .disableFollowRedirect
    .disableWarmUp
    // Uncomment to see Requests
    // .requestInfoExtractor(request => {
    //   println(request.getStringData)
    //   Nil
    //  })
    // Uncomment to see Response
    // .responseInfoExtractor(response => {
    //   println(response.getResponseBody)
    //   Nil
    //  })
    // .disableResponseChunksDiscarding

  val nbrNodes = 100000
  val nodesRange = 1 to nbrNodes
  val rnd = new scala.util.Random
  
  val chooseRandomNodes = exec((session) => {
    session.setAttributes(Map("from_ids" -> JSONArray.apply(List.fill(10)(nodesRange(rnd.nextInt(nodesRange.length)))).toString(),
                              "to_ids"   -> JSONArray.apply(List.fill(10)(nodesRange(rnd.nextInt(nodesRange.length)))).toString() ))

  })

  val createRelationship = """START node1=node({from_ids}), node2=node({to_ids}) CREATE UNIQUE node1-[:KNOWS]->node2"""
  val cypherQuery = """{"query": "%s", "params": {"from_ids": %s, "to_ids": %s } }""".format(createRelationship, "${from_ids}", "${to_ids}")


  val scn = scenario("Create Relationships")
    .during(10) {
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

