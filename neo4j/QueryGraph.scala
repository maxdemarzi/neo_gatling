import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import akka.util.duration._
import bootstrap._
import util.parsing.json.JSONArray


class QueryGraph extends Simulation {
  val httpConf = httpConfig
    .baseURL("http://localhost:7474")
    .acceptHeader("application/json")
    .responseInfoExtractor(response => {
      println(response.getResponseBody)
      Nil
    })
    .disableResponseChunksDiscarding

  val rnd = new scala.util.Random
  val nodeRange = 1 to 100000
  val chooseRandomNodes = exec((session) => {
    session.setAttribute("node_ids", JSONArray.apply(List.fill(10)(nodeRange(rnd.nextInt(nodeRange length)))).toString())
  })

  val getNodes = """START nodes=node({ids}) MATCH nodes -[:KNOWS]-> other_nodes RETURN ID(other_nodes)"""
  val cypherQuery = """{"query": "%s", "params": {"ids": %s}}""".format(getNodes, "${node_ids}")

  val scn = scenario("Query Graph")
    .during(30) {
    exec(chooseRandomNodes)
      .exec(
        http("query graph")
          .post("/db/data/cypher")
          .header("X-Stream", "true")
          .body(cypherQuery)
          .asJSON
          .check(status.is(200))
          .check(jsonPath("data")))
      .pause(0 milliseconds, 5 milliseconds)
  }

  setUp(
    scn.users(100).ramp(10).protocolConfig(httpConf)
  )
}

