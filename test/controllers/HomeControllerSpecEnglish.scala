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
import com.ideal.linked.toposoid.common.{TRANSVERSAL_STATE, TransversalState}
import com.ideal.linked.toposoid.knowledgebase.regist.model.{ImageReference, Knowledge, KnowledgeForImage, PropositionRelation, Reference}
import com.ideal.linked.toposoid.protocol.model.neo4j.Neo4jRecords
import com.ideal.linked.toposoid.protocol.model.parser.{KnowledgeForParser, KnowledgeSentenceSetForParser}
import com.ideal.linked.toposoid.sentence.transformer.neo4j.Sentence2Neo4jTransformer
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play.materializer
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.Helpers.{POST, contentType, defaultAwaitTimeout, status, _}
import play.api.test.{FakeRequest, _}
import io.jvm.uuid.UUID

class HomeControllerSpecEnglish extends PlaySpec with BeforeAndAfter with BeforeAndAfterAll with GuiceOneAppPerSuite  with Injecting with LazyLogging {

  val transversalState:String = Json.toJson(TransversalState(userId="test-user", username="guest", roleId=0, csrfToken = "")).toString()

  def registSingleClaim(knowledgeForParser:KnowledgeForParser): Unit = {
    val knowledgeSentenceSetForParser = KnowledgeSentenceSetForParser(
      List.empty[KnowledgeForParser],
      List.empty[PropositionRelation],
      List(knowledgeForParser),
      List.empty[PropositionRelation])
    Sentence2Neo4jTransformer.createGraph(knowledgeSentenceSetForParser, TransversalState(userId="test-user", username="guest", roleId=0, csrfToken = ""))
  }

  before {
    Neo4JAccessor.delete()
  }

  override def beforeAll(): Unit = {
    Neo4JAccessor.delete()
  }

  override def afterAll(): Unit = {
    Neo4JAccessor.delete()
  }

  val controller: HomeController = inject[HomeController]

  "An access of getQueryResult for English knowledge" should {
    "returns an appropriate response" in {
      registSingleClaim(KnowledgeForParser( UUID.random.toString, UUID.random.toString, Knowledge("Time is money.","en_US", "{}", false )))
      val fr = FakeRequest(POST, "/getQueryResult")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalState)
        .withJsonBody(Json.parse("""{ "query":"MATCH (n) WHERE n.lang='en_US' RETURN n ", "target": "" }"""))
      val result= call(controller.getQueryResult(), fr)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      assert(!contentAsString(result).equals(""))
    }
  }

  "An access of getQueryFormattedResult for English knowledge Nodes" should {
    "returns an appropriate response" in {
      registSingleClaim(KnowledgeForParser( UUID.random.toString, UUID.random.toString, Knowledge("Time is money.","en_US", "{}", false )))
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalState)
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

  "An access of getQueryFormattedResult for English knowledge Edges" should {
    "returns an appropriate response" in {
      registSingleClaim(KnowledgeForParser( UUID.random.toString, UUID.random.toString, Knowledge("Time is money.","en_US", "{}", false )))
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalState)
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

  "An access of getQueryFormattedResult for Synonym Nodes of English knowledge." should {
    "returns an appropriate response" in {
      registSingleClaim(KnowledgeForParser(UUID.random.toString, UUID.random.toString, Knowledge("He has a good chance.", "en_US", "{}", false)))
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalState)
        .withJsonBody(Json.parse("""{ "query":"MATCH (sn:SynonymNode{nodeName:'opportunity'})-[se:SynonymEdge]-(n:ClaimNode{surface:'chance'})  return sn, se, n", "target": "" }"""))
      val result = call(controller.getQueryFormattedResult(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
      assert(neo4jRecords.records.size == 1)
    }
  }

  "An access of getQueryFormattedResult for Synonym Edges of English knowledge." should {
    "returns an appropriate response" in {
      registSingleClaim(KnowledgeForParser(UUID.random.toString, UUID.random.toString, Knowledge("He has a good chance.", "en_US", "{}", false)))
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalState)
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

    "An access of getQueryFormattedResult for Image Nodes of English knowledge." should {
      "returns an appropriate response" in {
        val reference1 = Reference(url = "http://images.cocodataset.org/val2017/000000039769.jpg", surface = "cats", surfaceIndex = 3, isWholeSentence = false, originalUrlOrReference = "")
        val referenceImage1 = ImageReference(reference = reference1, x = 0, y = 0, width = 128, height = 128)
        val featureId1 = UUID.random.toString
        val knowledgeForImage1 = KnowledgeForImage(featureId1, referenceImage1)

        registSingleClaim(KnowledgeForParser(UUID.random.toString, UUID.random.toString, Knowledge("There are two cats.", "en_US", "{}", false, List(knowledgeForImage1))))
        val fr = FakeRequest(POST, "/getQueryFormattedResult")
          .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalState)
          .withJsonBody(Json.parse("""{ "query":"MATCH (in:ImageNode{url:'http://images.cocodataset.org/val2017/000000039769.jpg'})-[ie:ImageEdge]->(n:ClaimNode{surface:'cats'})  return in, ie, n", "target": "" }"""))
        val result = call(controller.getQueryFormattedResult(), fr)
        status(result) mustBe OK
        val jsonResult: String = contentAsJson(result).toString()
        val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
        assert(neo4jRecords.records.size == 1)
      }
    }

    "An access of getQueryFormattedResult for Image Edges of Japanese knowledge." should {
      "returns an appropriate response" in {
        val reference1 = Reference(url = "http://images.cocodataset.org/val2017/000000039769.jpg", surface = "cats", surfaceIndex = 3, isWholeSentence = false, originalUrlOrReference = "")
        val referenceImage1 = ImageReference(reference = reference1, x = 0, y = 0, width = 128, height = 128)
        val featureId1 = UUID.random.toString
        val knowledgeForImage1 = KnowledgeForImage(featureId1, referenceImage1)
        registSingleClaim(KnowledgeForParser(UUID.random.toString, UUID.random.toString, Knowledge("There are two cats.", "en_US", "{}", false, List(knowledgeForImage1))))
        val fr = FakeRequest(POST, "/getQueryFormattedResult")
          .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalState)
          .withJsonBody(Json.parse("""{ "query":"MATCH (in:ImageNode{url:'http://images.cocodataset.org/val2017/000000039769.jpg'})-[ie:ImageEdge]->(n:ClaimNode{surface:'猫が'})  return in, ie, n", "target": "" }"""))
        val result = call(controller.getQueryFormattedResult(), fr)
        status(result) mustBe OK
        val jsonResult: String = contentAsJson(result).toString()
        val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
        neo4jRecords.records.reverse.map(record => {
          record.map(x => {
            x.key match {
              case "in" => {
                x.value.featureNode match {
                  case Some(y) => assert(y.url.equals("http://images.cocodataset.org/val2017/000000039769.jpg"))
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
  }
}
