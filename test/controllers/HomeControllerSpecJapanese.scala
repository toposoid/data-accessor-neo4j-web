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
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, contentType, defaultAwaitTimeout, status}
import play.api.test.Helpers._
import play.api.test._
import scala.io.Source
//import io.jvm.uuid.UUID

class HomeControllerSpecJapanese extends PlaySpec with BeforeAndAfter with BeforeAndAfterAll with GuiceOneAppPerSuite  with Injecting with LazyLogging {

  val transversalState:TransversalState = TransversalState(userId="test-user", username="guest", roleId=0, csrfToken = "")
  val transversalStateJson:String = Json.toJson(transversalState).toString()
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


  "An access of executeQuery for registering Japanese knowledge1" should {
    "returns an appropriate response" in {
      val query:String =Source.fromResource("query_jp_1.txt").mkString.stripMargin
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

  "An access of getQueryFormattedResult for Nodes of Japanese knowledge1." should {
    "returns an appropriate response" in {
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse("""{ "query":"MATCH (n) WHERE n.lang='ja_JP' RETURN n", "target": "" }"""))
      val result = call(controller.getQueryFormattedResult(), fr)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      val jsonResult: String = contentAsJson(result).toString()
      val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
      val sentenceMap: List[(Int, String)] = neo4jRecords.records.reverse.foldLeft(List.empty[(Int, String)]) {
        (acc, record) => {
          val records = record.filter(x => x.key.equals("n"))
          val data = records.foldLeft(List.empty[(Int, String)]){
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
      val sentence: String = sentenceMap.toSeq.sortBy(_._1).foldLeft("") { (acc, x) => acc + x._2 }
      assert(sentence.equals("案ずるより産むが易し。"))

    }
  }

  "An access of getQueryFormattedResult for Edges of Japanese knowledge1." should {
    "returns an appropriate response" in {
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse("""{ "query":"MATCH (n:ClaimNode)-[e:LocalEdge]-(m:ClaimNode{isMainSection:'true'}) WHERE n.lang='ja_JP' return n, e, m", "target": "" }"""))

      val result = call(controller.getQueryFormattedResult(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
      assert(neo4jRecords.records.size == 1)
      neo4jRecords.records.reverse.map( record => {
          record.map(x => {
            x.key match {
              case "n" => {
                x.value.localNode match {
                  case Some(y) => assert(y.predicateArgumentStructure.surface.equals("産むが"))
                  case _ => assert(false)
                }
              }
              case "e" => {
                x.value.localEdge match {
                  case Some(y) => assert(y.caseStr.equals("連用"))
                  case _ => assert(false)
                }
              }
              case "m" => {
                x.value.localNode match {
                  case Some(y) => assert(y.predicateArgumentStructure.surface.equals("易し。"))
                  case _ => assert(false)
                }
              }
              case _ => assert(false)
            }
          })
        })
    }
  }
  

  "An access of executeQuery for registering Japanese knowledge2" should {
    "returns an appropriate response" in {
      val query:String =Source.fromResource("query_jp_2.txt").mkString.stripMargin
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



  "An access of getQueryFormattedResult for Synonym Nodes of Japanese knowledge2." should {
    "returns an appropriate response" in {
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse("""{ "query":"MATCH (sn:SynonymNode{nodeName:'御結び'})-[se:SynonymEdge]-(n:ClaimNode{surface:'おにぎりを'})  return sn, se, n", "target": "" }"""))
      val result = call(controller.getQueryFormattedResult(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
      assert(neo4jRecords.records.size == 1)
    }
  }


  "An access of getQueryFormattedResult for Synonym Edges of Japanese knowledge2." should {
    "returns an appropriate response" in {
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse("""{ "query":"MATCH (sn:SynonymNode{nodeName:'御結び'})-[se:SynonymEdge]-(n:ClaimNode{surface:'おにぎりを'})  return sn, se, n", "target": "" }"""))
      val result = call(controller.getQueryFormattedResult(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]

      neo4jRecords.records.reverse.map(record => {
        record.map(x => {
          x.key match {
            case "sn" => {
              x.value.synonymNode match {
                case Some(y) => assert(y.nodeName.equals("御結び"))
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
                case Some(y) => assert(y.predicateArgumentStructure.surface.equals("おにぎりを"))
                case _ => assert(false)
              }
            }
            case _ => assert(false)
          }
        })
      })
    }
  }
  
  "An access of executeQuery for registering Japanese knowledge3" should {
    "returns an appropriate response" in {
      val query:String =Source.fromResource("query_jp_3.txt").mkString.stripMargin
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

  "An access of getQueryFormattedResult for Image Nodes of Japanese knowledge3." should {
    "returns an appropriate response" in {
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse("""{ "query":"MATCH (in:ImageNode{source:'http://images.cocodataset.org/val2017/000000039769.jpg'})-[ie:ImageEdge]->(n:ClaimNode{surface:'猫が'})  return in, ie, n", "target": "" }"""))
      val result = call(controller.getQueryFormattedResult(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
      assert(neo4jRecords.records.size == 1)
    }
  }

  "An access of getQueryFormattedResult for Image Edges of Japanese knowledge3." should {
    "returns an appropriate response" in {
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse("""{ "query":"MATCH (in:ImageNode{source:'http://images.cocodataset.org/val2017/000000039769.jpg'})-[ie:ImageEdge]->(n:ClaimNode{surface:'猫が'})  return in, ie, n", "target": "" }"""))
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
                case Some(y) => assert(y.predicateArgumentStructure.surface.equals("猫が"))
                case _ => assert(false)
              }
            }
            case _ => assert(false)
          }
        })
      })
    }
  }

  "An access of executeQuery for registering Japanese knowledge4" should {
    "returns an appropriate response" in {
      val query:String =Source.fromResource("query_jp_4.txt").mkString.stripMargin
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
        .withJsonBody(Json.parse("""{ "query":"MATCH x = (:SemiGlobalClaimNode{sentence:'これはドキュメント用のテストです。'}) RETURN x", "target": "" }"""))
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
        .withJsonBody(Json.parse("""{ "query":"MATCH x = (:GlobalNode{titleOfTopPage:'テストタイトル'}) RETURN x", "target": "" }"""))
      val result = call(controller.getQueryFormattedResult(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
      assert(neo4jRecords.records.size == 1)
    }
  }
  
}
