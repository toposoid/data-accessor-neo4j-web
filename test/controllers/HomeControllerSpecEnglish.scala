/*
 * Copyright (C) 2025  Linked Ideal LLC.[https://linked-ideal.com/]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package controllers

import com.ideal.linked.data.accessor.neo4j.Neo4JAccessor
import com.ideal.linked.toposoid.common.{TRANSVERSAL_STATE, ToposoidUtils, TransversalState}
import com.ideal.linked.toposoid.knowledgebase.regist.model.{DocumentPageReference, ImageReference, Knowledge, KnowledgeForDocument, KnowledgeForImage, PropositionRelation, Reference}
import com.ideal.linked.toposoid.protocol.model.neo4j.Neo4jRecords
import com.ideal.linked.toposoid.protocol.model.parser.{KnowledgeForParser, KnowledgeSentenceSetForParser}
//import com.ideal.linked.toposoid.test.utils.TestUtils
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play.materializer
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.Helpers.{POST, contentType, defaultAwaitTimeout, status, _}
import play.api.test.{FakeRequest, _}
//import io.jvm.uuid.UUID
import scala.io.Source

class HomeControllerSpecEnglish extends PlaySpec with BeforeAndAfter with BeforeAndAfterAll with GuiceOneAppPerSuite  with Injecting with LazyLogging {

  val transversalState: TransversalState = TransversalState(userId = "test-user", username = "guest", roleId = 0, csrfToken = "")
  val transversalStateJson: String = Json.toJson(transversalState).toString()
  val neo4JUtils = new Neo4JUtilsImpl()

  /*
  before {
    Neo4JAccessor.delete()
  }
  */
  override def beforeAll(): Unit = {
    Neo4JAccessor.delete()
  }

  override def afterAll(): Unit = {
    Neo4JAccessor.delete()
  }

  val controller: HomeController = inject[HomeController]

  "An access of executeQuery for registering English knowledge1" should {
    "returns an appropriate response" in {
      val query: String = Source.fromResource("query_en_1.txt").mkString.stripMargin
      val convertQuery = ToposoidUtils.encodeJsonInJson(query)
      val json = s"""{ "query":"$convertQuery", "target": "" }"""
      val fr = FakeRequest(POST, "/executeQuery")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse(json))
      val result = call(controller.executeQuery(), fr)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      assert(contentAsString(result).equals("""{"status":"OK","message":""}"""))
    }
  }

  "An access of getQueryFormattedResult for English knowledge1 Nodes" should {
    "returns an appropriate response" in {
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse("""{ "query":"MATCH (n) WHERE n.lang='en_US' RETURN n", "target": "" }"""))
      val result = call(controller.getQueryFormattedResult(), fr)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      val jsonResult: String = contentAsJson(result).toString()
      val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
      val sentenceMap: List[(Int, String)] = neo4jRecords.records.reverse.foldLeft(List.empty[(Int, String)]) {
        (acc, record) => {
          val records = record.filter(x => x.key.equals("n"))
          val data = records.foldLeft(List.empty[(Int, String)]) {
            (acc2, y) => {
              y.value.localNode match {
                case Some(z) => acc2 :+ (z.predicateArgumentStructure.currentId, z.predicateArgumentStructure.surface)
                case _ => acc2
              }
            }
          }
          acc ::: data
        }
      }
      val sentence: String = sentenceMap.toSeq.sortBy(_._1).foldLeft("") { (acc, x) => acc + " " + x._2 }
      assert(sentence.trim.equals("Time is money ."))
    }
  }

  "An access of getQueryFormattedResult for English knowledge1 Edges" should {
    "returns an appropriate response" in {
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse("""{ "query":"MATCH (n:ClaimNode)-[e:LocalEdge]-(m:ClaimNode{caseType:'attr'}) WHERE n.lang='en_US'  return n, e, m", "target": "" }"""))

      val result = call(controller.getQueryFormattedResult(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
      assert(neo4jRecords.records.size == 1)
      neo4jRecords.records.reverse.map(record => {
        record.map(x => {
          x.key match {
            case "n" => {
              x.value.localNode match {
                case Some(y) => assert(y.predicateArgumentStructure.surface.equals("is"))
                case _ => assert(false)
              }
            }
            case "e" => {
              x.value.localEdge match {
                case Some(y) => assert(y.caseStr.equals("attr"))
                case _ => assert(false)
              }
            }
            case "m" => {
              x.value.localNode match {
                case Some(y) => assert(y.predicateArgumentStructure.surface.equals("money"))
                case _ => assert(false)
              }
            }
            case _ => assert(false)
          }
        })
      })
    }
  }

  "An access of executeQuery for registering English knowledge2" should {
    "returns an appropriate response" in {
      val query: String = Source.fromResource("query_en_2.txt").mkString.stripMargin
      val convertQuery = ToposoidUtils.encodeJsonInJson(query)
      val json = s"""{ "query":"$convertQuery", "target": "" }"""
      val fr = FakeRequest(POST, "/executeQuery")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse(json))
      val result = call(controller.executeQuery(), fr)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      assert(contentAsString(result).equals("""{"status":"OK","message":""}"""))
    }
  }
  
  "An access of getQueryFormattedResult2 for English knowledge2 Nodes" should {
    "returns an appropriate response" in {
      val query = """MATCH x = (:ClaimNode{surface:'That'})-[:LocalEdge]->(:ClaimNode{surface:"'s"})<-[:LocalEdge]-(:ClaimNode{surface:'life'}) RETURN x"""
      val convertQuery = ToposoidUtils.encodeJsonInJson(query)
      val json = s"""{ "query":"$convertQuery", "target": "" }"""
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse(json))
      val result = call(controller.getQueryFormattedResult(), fr)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      val jsonResult: String = contentAsJson(result).toString()
      val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
      assert(neo4jRecords.records.size == 1)

      val fr2 = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse("""{ "query":"MATCH (n) WHERE n.sentenceId='b2753e05-30ee-45eb-9c40-e6a4ce214d35' AND n.lang='en_US' RETURN n", "target": "" }"""))
      val result2 = call(controller.getQueryFormattedResult(), fr2)
      status(result2) mustBe OK
      contentType(result2) mustBe Some("application/json")
      val jsonResult2: String = contentAsJson(result2).toString()
      val neo4jRecords2: Neo4jRecords = Json.parse(jsonResult2).as[Neo4jRecords]

      val sentenceMap: List[(Int, String)] = neo4jRecords2.records.reverse.foldLeft(List.empty[(Int, String)]) {
        (acc, record) => {
          val records = record.filter(x => x.key.equals("n"))
          val data = records.foldLeft(List.empty[(Int, String)]) {
            (acc2, y) => {
              y.value.localNode match {
                case Some(z) => acc2 :+ (z.predicateArgumentStructure.currentId, z.predicateArgumentStructure.surface)
                case _ => acc2
              }
            }
          }
          acc ::: data
        }
      }
      val sentence: String = sentenceMap.toSeq.sortBy(_._1).foldLeft("") { (acc, x) => acc + " " + x._2 }
      assert(sentence.trim.equals("That 's life ."))
    }
  }

  "An access of executeQuery for registering English knowledge3" should {
    "returns an appropriate response" in {
      val query: String = Source.fromResource("query_en_3.txt").mkString.stripMargin
      val convertQuery = ToposoidUtils.encodeJsonInJson(query)
      val json = s"""{ "query":"$convertQuery", "target": "" }"""
      val fr = FakeRequest(POST, "/executeQuery")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse(json))
      val result = call(controller.executeQuery(), fr)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      assert(contentAsString(result).equals("""{"status":"OK","message":""}"""))
    }
  }

  "An access of getQueryFormattedResult for Synonym Nodes of English knowledge3." should {
    "returns an appropriate response" in {
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse("""{ "query":"MATCH (sn:SynonymNode{nodeName:'opportunity'})-[se:SynonymEdge]-(n:ClaimNode{surface:'chance'})  return sn, se, n", "target": "" }"""))
      val result = call(controller.getQueryFormattedResult(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
      assert(neo4jRecords.records.size == 1)
    }
  }

  "An access of getQueryFormattedResult for Synonym Edges of English knowledge3." should {
    "returns an appropriate response" in {
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse("""{ "query":"MATCH (sn:SynonymNode{nodeName:'opportunity'})-[se:SynonymEdge]-(n:ClaimNode{surface:'chance'})  return sn, se, n", "target": "" }"""))
      val result = call(controller.getQueryFormattedResult(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]

      neo4jRecords.records.reverse.map(record => {
        record.map(x => {
          x.key match {
            case "sn" => {
              x.value.synonymNode match {
                case Some(y) => assert(y.nodeName.equals("opportunity"))
                case _ => assert(false)
              }
            }
            case "se" => {
              x.value.synonymEdge match {
                case Some(y) => assert(y.similality.isInstanceOf[Float])
                case _ => assert(false)
              }
            }
            case "n" => {
              x.value.localNode match {
                case Some(y) => assert(y.predicateArgumentStructure.surface.equals("chance"))
                case _ => assert(false)
              }
            }
            case _ => assert(false)
          }
        })
      })
    }
  }
  

  "An access of executeQuery for registering English knowledge4" should {
    "returns an appropriate response" in {
      val query: String = Source.fromResource("query_en_4.txt").mkString.stripMargin
      val convertQuery = ToposoidUtils.encodeJsonInJson(query)
      val json = s"""{ "query":"$convertQuery", "target": "" }"""
      val fr = FakeRequest(POST, "/executeQuery")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse(json))
      val result = call(controller.executeQuery(), fr)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      assert(contentAsString(result).equals("""{"status":"OK","message":""}"""))
    }
  }


  "An access of getQueryFormattedResult for Image Nodes of English knowledge4." should {
      "returns an appropriate response" in {
        val fr = FakeRequest(POST, "/getQueryFormattedResult")
          .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
          .withJsonBody(Json.parse("""{ "query":"MATCH (in:ImageNode{source:'http://images.cocodataset.org/val2017/000000039769.jpg'})-[ie:ImageEdge]->(n:ClaimNode{surface:'cats'})  return in, ie, n", "target": "" }"""))
        val result = call(controller.getQueryFormattedResult(), fr)
        status(result) mustBe OK
        val jsonResult: String = contentAsJson(result).toString()
        val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
        assert(neo4jRecords.records.size == 1)
      }
    }

    "An access of getQueryFormattedResult for Image Edges of English knowledge4." should {
      "returns an appropriate response" in {
        val fr = FakeRequest(POST, "/getQueryFormattedResult")
          .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
          .withJsonBody(Json.parse("""{ "query":"MATCH (in:ImageNode{source:'http://images.cocodataset.org/val2017/000000039769.jpg'})-[ie:ImageEdge]->(n:ClaimNode{surface:'cats'})  return in, ie, n", "target": "" }"""))
        val result = call(controller.getQueryFormattedResult(), fr)
        status(result) mustBe OK
        val jsonResult: String = contentAsJson(result).toString()
        val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
        neo4jRecords.records.reverse.map(record => {
          record.map(x => {
            x.key match {
              case "in" => {
                x.value.featureNode match {
                  case Some(y) => assert(y.source.equals("http://images.cocodataset.org/val2017/000000039769.jpg"))
                  case _ => assert(false)
                }
              }
              case "ie" => {
                x.value.featureEdge match {
                  case Some(y) => assert(true)
                  case _ => assert(false)
                }
              }
              case "n" => {
                x.value.localNode match {
                  case Some(y) => assert(y.predicateArgumentStructure.surface.equals("cats"))
                  case _ => assert(false)
                }
              }
              case _ => assert(false)
            }
          })
        })
      }
    }

  "An access of executeQuery for registering English knowledge4a" should {
    "returns an appropriate response" in {
      val query: String = Source.fromResource("query_en_4a.txt").mkString.stripMargin
      val convertQuery = ToposoidUtils.encodeJsonInJson(query)
      val json = s"""{ "query":"$convertQuery", "target": "" }"""
      val fr = FakeRequest(POST, "/executeQuery")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse(json))
      val result = call(controller.executeQuery(), fr)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      assert(contentAsString(result).equals("""{"status":"OK","message":""}"""))
    }
  }


  "An access of getQueryFormattedResult for Table Nodes of English knowledge4a." should {
      "returns an appropriate response" in {
        val fr = FakeRequest(POST, "/getQueryFormattedResult")
          .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
          .withJsonBody(Json.parse("""{ "query":"MATCH (in:TableNode{source:'https://www.e-stat.go.jp/stat-search/file-download?statInfId=000040292480&fileKind=1'})-[ie:TableEdge]->(n:ClaimNode{surface:'Figure1'})  return in, ie, n", "target": "" }"""))
        val result = call(controller.getQueryFormattedResult(), fr)
        status(result) mustBe OK
        val jsonResult: String = contentAsJson(result).toString()
        val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
        assert(neo4jRecords.records.size == 1)
      }
    }

    "An access of getQueryFormattedResult for Table Edges of English knowledge4a." should {
      "returns an appropriate response" in {
        val fr = FakeRequest(POST, "/getQueryFormattedResult")
          .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
          .withJsonBody(Json.parse("""{ "query":"MATCH (in:TableNode{source:'https://www.e-stat.go.jp/stat-search/file-download?statInfId=000040292480&fileKind=1'})-[ie:TableEdge]->(n:ClaimNode{surface:'Figure1'})  return in, ie, n", "target": "" }"""))
        val result = call(controller.getQueryFormattedResult(), fr)
        status(result) mustBe OK
        val jsonResult: String = contentAsJson(result).toString()
        val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
        neo4jRecords.records.reverse.map(record => {
          record.map(x => {
            x.key match {
              case "in" => {
                x.value.featureNode match {
                  case Some(y) => assert(y.source.equals("https://www.e-stat.go.jp/stat-search/file-download?statInfId=000040292480&fileKind=1"))
                  case _ => assert(false)
                }
              }
              case "ie" => {
                x.value.featureEdge match {
                  case Some(y) => assert(true)
                  case _ => assert(false)
                }
              }
              case "n" => {
                x.value.localNode match {
                  case Some(y) => assert(y.predicateArgumentStructure.surface.equals("Figure1"))
                  case _ => assert(false)
                }
              }
              case _ => assert(false)
            }
          })
        })
      }
    }



  "An access of executeQuery for registering English knowledge5" should {
    "returns an appropriate response" in {
      val query: String = Source.fromResource("query_en_5.txt").mkString.stripMargin
      val convertQuery = ToposoidUtils.encodeJsonInJson(query)
      val json = s"""{ "query":"$convertQuery", "target": "" }"""
      val fr = FakeRequest(POST, "/executeQuery")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse(json))
      val result = call(controller.executeQuery(), fr)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      assert(contentAsString(result).equals("""{"status":"OK","message":""}"""))
    }
  }


  "The Document-Semi-GlobalNode-Test." should {
      "returns an appropriate response" in {
        val fr = FakeRequest(POST, "/getQueryFormattedResult")
          .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
          .withJsonBody(Json.parse("""{ "query":"MATCH x = (:SemiGlobalClaimNode{sentence:'This is a documentation test.'}) RETURN x", "target": "" }"""))
        val result = call(controller.getQueryFormattedResult(), fr)
        status(result) mustBe OK
        val jsonResult: String = contentAsJson(result).toString()
        val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
        assert(neo4jRecords.records.size == 1)
      }
  }

  "The Document-GlobalNode-Test." should {
      "returns an appropriate response" in {
        val fr = FakeRequest(POST, "/getQueryFormattedResult")
          .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
          .withJsonBody(Json.parse("""{ "query":"MATCH x = (:GlobalNode{titleOfTopPage:'TestTitle'}) RETURN x", "target": "" }"""))
        val result = call(controller.getQueryFormattedResult(), fr)
        status(result) mustBe OK
        val jsonResult: String = contentAsJson(result).toString()
        val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
        assert(neo4jRecords.records.size == 1)
      }
  }

}
