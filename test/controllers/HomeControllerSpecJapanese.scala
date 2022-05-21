/*
 * Copyright 2021 Linked Ideal LLC.[https://linked-ideal.com/]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import com.ideal.linked.data.accessor.neo4j.Neo4JAccessor
import com.ideal.linked.toposoid.knowledgebase.regist.model.Knowledge
import com.ideal.linked.toposoid.protocol.model.neo4j.Neo4jRecords
import com.ideal.linked.toposoid.sentence.transformer.neo4j.Sentence2Neo4jTransformer
import com.typesafe.scalalogging.{LazyLogging}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play.materializer
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, contentType, defaultAwaitTimeout, status}
import play.api.test.Helpers._
import play.api.test._

class HomeControllerSpecJapanese extends PlaySpec with BeforeAndAfter with BeforeAndAfterAll with GuiceOneAppPerSuite  with Injecting with LazyLogging {

  override def beforeAll(): Unit = {
    Neo4JAccessor.delete()
    Sentence2Neo4jTransformer.createGraphAuto(List(Knowledge("案ずるより産むが易し。","ja_JP", "{}", false )))
  }

  override def afterAll(): Unit = {
    Neo4JAccessor.delete()
  }

  val controller: HomeController = inject[HomeController]

  "An access of getQueryResult for Japanese knowledge" should {
    "returns an appropriate response" in {
      val fr = FakeRequest(POST, "/getQueryResult")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse("""{ "query":"MATCH (n) WHERE n.lang='ja_JP' RETURN n ", "target": "" }"""))
      val result= call(controller.getQueryResult(), fr)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      assert(!contentAsString(result).equals(""))
    }
  }

  "An access of getQueryFormattedResult for Japanese knowledge Nodes" should {
    "returns an appropriate response" in {
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse("""{ "query":"MATCH (n) WHERE n.lang='ja_JP' RETURN n", "target": "" }"""))
      val result = call(controller.getQueryFormattedResult(), fr)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      val jsonResult: String = contentAsJson(result).toString()
      val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
      val sentenceMap: List[(Int, String)] = neo4jRecords.records.reverse.map(record => {
        record.filter(x => x.key.equals("n")).map(y =>
          y.value.logicNode.currentId -> y.value.logicNode.surface
        ).head
      })
      val sentence: String = sentenceMap.toSeq.sortBy(_._1).foldLeft("") { (acc, x) => acc + x._2 }
      assert(sentence.equals("案ずるより産むが易し。"))
    }
  }

  "An access of getQueryFormattedResult for Japanese knowledge Edges" should {
    "returns an appropriate response" in {
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse("""{ "query":"MATCH (n:ClaimNode)-[e:ClaimEdge]-(m:ClaimNode{isMainSection:'true'}) WHERE n.lang='ja_JP' return n, e, m", "target": "" }"""))

      val result = call(controller.getQueryFormattedResult(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
      assert(neo4jRecords.records.size == 1)
      neo4jRecords.records.reverse.map(record => {
        record.map(x => {
          x.key match {
            case "n" => assert(x.value.logicNode.surface.equals("産むが"))
            case "e" => assert(x.value.logicEdge.caseStr.equals("連用"))
            case "m" => assert(x.value.logicNode.surface.equals("易し。"))
          }
        })
      })
    }
  }
}
