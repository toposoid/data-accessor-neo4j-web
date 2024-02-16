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
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, contentType, defaultAwaitTimeout, status}
import play.api.test.Helpers._
import play.api.test._
import io.jvm.uuid.UUID

class HomeControllerSpecJapanese extends PlaySpec with BeforeAndAfter with BeforeAndAfterAll with GuiceOneAppPerSuite  with Injecting with LazyLogging {

  def registSingleClaim(knowledgeForParser:KnowledgeForParser): Unit = {
    val knowledgeSentenceSetForParser = KnowledgeSentenceSetForParser(
      List.empty[KnowledgeForParser],
      List.empty[PropositionRelation],
      List(knowledgeForParser),
      List.empty[PropositionRelation])
    Sentence2Neo4jTransformer.createGraph(knowledgeSentenceSetForParser)
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

  "An access of getQueryResult for Japanese knowledge" should {
    "returns an appropriate response" in {
      registSingleClaim(KnowledgeForParser( UUID.random.toString, UUID.random.toString, Knowledge("案ずるより産むが易し。","ja_JP", "{}", false )))
      val fr = FakeRequest(POST, "/getQueryResult")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse("""{ "query":"MATCH (n) WHERE n.lang='ja_JP' RETURN n ", "target": "" }"""))
      val result= call(controller.getQueryResult(), fr)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      assert(!contentAsString(result).equals(""))
    }
  }

  "An access of getQueryFormattedResult for Nodes of Japanese knowledge." should {
    "returns an appropriate response" in {
      registSingleClaim(KnowledgeForParser( UUID.random.toString, UUID.random.toString, Knowledge("案ずるより産むが易し。","ja_JP", "{}", false )))
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json")
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

  "An access of getQueryFormattedResult for Edges of Japanese knowledge." should {
    "returns an appropriate response" in {
      registSingleClaim(KnowledgeForParser( UUID.random.toString, UUID.random.toString, Knowledge("案ずるより産むが易し。","ja_JP", "{}", false )))
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json")
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

  "An access of getQueryFormattedResult for Synonym Nodes of Japanese knowledge." should {
    "returns an appropriate response" in {
      registSingleClaim(KnowledgeForParser( UUID.random.toString, UUID.random.toString, Knowledge("彼はおにぎりを購入した。","ja_JP", "{}", false )))
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse("""{ "query":"MATCH (sn:SynonymNode{nodeName:'御結び'})-[se:SynonymEdge]-(n:ClaimNode{surface:'おにぎりを'})  return sn, se, n", "target": "" }"""))
      val result = call(controller.getQueryFormattedResult(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
      assert(neo4jRecords.records.size == 1)
    }
  }


  "An access of getQueryFormattedResult for Synonym Edges of Japanese knowledge." should {
    "returns an appropriate response" in {
      registSingleClaim(KnowledgeForParser(UUID.random.toString, UUID.random.toString, Knowledge("彼はおにぎりを購入した。", "ja_JP", "{}", false)))
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json")
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

  "An access of getQueryFormattedResult for Image Nodes of Japanese knowledge." should {
    "returns an appropriate response" in {
      val reference1 = Reference(url = "http://images.cocodataset.org/val2017/000000039769.jpg", surface = "猫が", surfaceIndex = 0, isWholeSentence = false, originalUrlOrReference = "")
      val referenceImage1 = ImageReference(reference = reference1, x = 0, y = 0, wi = 128, height = 128)
      val featureId1 = UUID.random.toString
      val knowledgeForImage1 = KnowledgeForImage(featureId1, referenceImage1)

      registSingleClaim(KnowledgeForParser(UUID.random.toString, UUID.random.toString, Knowledge("猫が２匹います。", "ja_JP", "{}", false, List(knowledgeForImage1))))
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse("""{ "query":"MATCH (in:ImageNode{url:'http://images.cocodataset.org/val2017/000000039769.jpg'})-[ie:ImageEdge]->(n:ClaimNode{surface:'猫が'})  return in, ie, n", "target": "" }"""))
      val result = call(controller.getQueryFormattedResult(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val neo4jRecords: Neo4jRecords = Json.parse(jsonResult).as[Neo4jRecords]
      assert(neo4jRecords.records.size == 1)
    }
  }

  "An access of getQueryFormattedResult for Image Edges of Japanese knowledge." should {
    "returns an appropriate response" in {
      val reference1 = Reference(url = "http://images.cocodataset.org/val2017/000000039769.jpg", surface = "猫が", surfaceIndex = 0, isWholeSentence = false, originalUrlOrReference = "")
      val referenceImage1 = ImageReference(reference = reference1, x = 0, y = 0, weight = 128, height = 128)
      val featureId1 = UUID.random.toString
      val knowledgeForImage1 = KnowledgeForImage(featureId1, referenceImage1)

      registSingleClaim(KnowledgeForParser(UUID.random.toString, UUID.random.toString, Knowledge("猫が２匹います。", "ja_JP", "{}", false, List(knowledgeForImage1))))
      val fr = FakeRequest(POST, "/getQueryFormattedResult")
        .withHeaders("Content-type" -> "application/json")
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
}
