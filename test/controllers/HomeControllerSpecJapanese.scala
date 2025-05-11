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
import com.ideal.linked.toposoid.common.{TRANSVERSAL_STATE, ToposoidUtils, TransversalState}
import com.ideal.linked.toposoid.knowledgebase.regist.model.{DocumentPageReference, ImageReference, Knowledge, KnowledgeForDocument, KnowledgeForImage, PropositionRelation, Reference}
import com.ideal.linked.toposoid.protocol.model.neo4j.Neo4jRecords
import com.ideal.linked.toposoid.protocol.model.parser.{KnowledgeForParser, KnowledgeSentenceSetForParser}
import com.ideal.linked.toposoid.test.utils.TestUtils
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

  val transversalState:TransversalState = TransversalState(userId="test-user", username="guest", roleId=0, csrfToken = "")
  val transversalStateJson:String = Json.toJson(transversalState).toString()
  val neo4JUtils = new Neo4JUtilsImpl()

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


  "An access of executeQuery for registering Japanese knowledge" should {
    "returns an appropriate response" in {

      val query:String ="""
          |MERGE (:ClaimNode {nodeName: '易い', nodeId:'8847608c-533c-4846-ae38-e31e795f21a0-2', propositionId:'ceb278e8-ac27-4d44-9b63-00f091748e34', sentenceId:'8847608c-533c-4846-ae38-e31e795f21a0', currentId:'2', parentId:'-1', isMainSection:'true', surface:'易し。', normalizedName:'易い', dependType:'D', caseType:'文末', namedEntity:'', rangeExpressions:'{"":{}}', categories:'{"":""}', domains:'{"":""}', knowledgeFeatureReferences:'[]', isDenialWord:'false',isConditionalConnection:'false',normalizedNameYomi:'やすい?易しい',surfaceYomi:'やすし。',modalityType:'-',logicType:'-',morphemes:'["形容詞,*,イ形容詞アウオ段,文語基本形","特殊,句点,*,*"]',lang:'ja_JP'})
          |MERGE (:SynonymNode {nodeId:'易く_8847608c-533c-4846-ae38-e31e795f21a0-2', nodeName:'易く', propositionId:'ceb278e8-ac27-4d44-9b63-00f091748e34', sentenceId:'8847608c-533c-4846-ae38-e31e795f21a0'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: '易く_8847608c-533c-4846-ae38-e31e795f21a0-2'}), (d:ClaimNode {nodeId: '8847608c-533c-4846-ae38-e31e795f21a0-2'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:SynonymNode {nodeId:'づらい_8847608c-533c-4846-ae38-e31e795f21a0-2', nodeName:'づらい', propositionId:'ceb278e8-ac27-4d44-9b63-00f091748e34', sentenceId:'8847608c-533c-4846-ae38-e31e795f21a0'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: 'づらい_8847608c-533c-4846-ae38-e31e795f21a0-2'}), (d:ClaimNode {nodeId: '8847608c-533c-4846-ae38-e31e795f21a0-2'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:SynonymNode {nodeId:'難い_8847608c-533c-4846-ae38-e31e795f21a0-2', nodeName:'難い', propositionId:'ceb278e8-ac27-4d44-9b63-00f091748e34', sentenceId:'8847608c-533c-4846-ae38-e31e795f21a0'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: '難い_8847608c-533c-4846-ae38-e31e795f21a0-2'}), (d:ClaimNode {nodeId: '8847608c-533c-4846-ae38-e31e795f21a0-2'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:SynonymNode {nodeId:'やすく_8847608c-533c-4846-ae38-e31e795f21a0-2', nodeName:'やすく', propositionId:'ceb278e8-ac27-4d44-9b63-00f091748e34', sentenceId:'8847608c-533c-4846-ae38-e31e795f21a0'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: 'やすく_8847608c-533c-4846-ae38-e31e795f21a0-2'}), (d:ClaimNode {nodeId: '8847608c-533c-4846-ae38-e31e795f21a0-2'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:SynonymNode {nodeId:'づらかっ_8847608c-533c-4846-ae38-e31e795f21a0-2', nodeName:'づらかっ', propositionId:'ceb278e8-ac27-4d44-9b63-00f091748e34', sentenceId:'8847608c-533c-4846-ae38-e31e795f21a0'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: 'づらかっ_8847608c-533c-4846-ae38-e31e795f21a0-2'}), (d:ClaimNode {nodeId: '8847608c-533c-4846-ae38-e31e795f21a0-2'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:SynonymNode {nodeId:'やすかっ_8847608c-533c-4846-ae38-e31e795f21a0-2', nodeName:'やすかっ', propositionId:'ceb278e8-ac27-4d44-9b63-00f091748e34', sentenceId:'8847608c-533c-4846-ae38-e31e795f21a0'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: 'やすかっ_8847608c-533c-4846-ae38-e31e795f21a0-2'}), (d:ClaimNode {nodeId: '8847608c-533c-4846-ae38-e31e795f21a0-2'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:SynonymNode {nodeId:'にくい_8847608c-533c-4846-ae38-e31e795f21a0-2', nodeName:'にくい', propositionId:'ceb278e8-ac27-4d44-9b63-00f091748e34', sentenceId:'8847608c-533c-4846-ae38-e31e795f21a0'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: 'にくい_8847608c-533c-4846-ae38-e31e795f21a0-2'}), (d:ClaimNode {nodeId: '8847608c-533c-4846-ae38-e31e795f21a0-2'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:SynonymNode {nodeId:'にくく_8847608c-533c-4846-ae38-e31e795f21a0-2', nodeName:'にくく', propositionId:'ceb278e8-ac27-4d44-9b63-00f091748e34', sentenceId:'8847608c-533c-4846-ae38-e31e795f21a0'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: 'にくく_8847608c-533c-4846-ae38-e31e795f21a0-2'}), (d:ClaimNode {nodeId: '8847608c-533c-4846-ae38-e31e795f21a0-2'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:SynonymNode {nodeId:'にくかっ_8847608c-533c-4846-ae38-e31e795f21a0-2', nodeName:'にくかっ', propositionId:'ceb278e8-ac27-4d44-9b63-00f091748e34', sentenceId:'8847608c-533c-4846-ae38-e31e795f21a0'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: 'にくかっ_8847608c-533c-4846-ae38-e31e795f21a0-2'}), (d:ClaimNode {nodeId: '8847608c-533c-4846-ae38-e31e795f21a0-2'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:SynonymNode {nodeId:'やすい_8847608c-533c-4846-ae38-e31e795f21a0-2', nodeName:'やすい', propositionId:'ceb278e8-ac27-4d44-9b63-00f091748e34', sentenceId:'8847608c-533c-4846-ae38-e31e795f21a0'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: 'やすい_8847608c-533c-4846-ae38-e31e795f21a0-2'}), (d:ClaimNode {nodeId: '8847608c-533c-4846-ae38-e31e795f21a0-2'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:ClaimNode {nodeName: '産む', nodeId:'8847608c-533c-4846-ae38-e31e795f21a0-1', propositionId:'ceb278e8-ac27-4d44-9b63-00f091748e34', sentenceId:'8847608c-533c-4846-ae38-e31e795f21a0', currentId:'1', parentId:'2', isMainSection:'false', surface:'産むが', normalizedName:'産む', dependType:'D', caseType:'連用', namedEntity:'', rangeExpressions:'{"":{}}', categories:'{"":""}', domains:'{"産む":"家庭・暮らし"}', knowledgeFeatureReferences:'[]', isDenialWord:'false',isConditionalConnection:'false',normalizedNameYomi:'うむ',surfaceYomi:'うむが',modalityType:'-',logicType:'-',morphemes:'["動詞,*,子音動詞マ行,基本形","助詞,接続助詞,*,*"]',lang:'ja_JP'})
          |MERGE (:SynonymNode {nodeId:'儲ける_8847608c-533c-4846-ae38-e31e795f21a0-1', nodeName:'儲ける', propositionId:'ceb278e8-ac27-4d44-9b63-00f091748e34', sentenceId:'8847608c-533c-4846-ae38-e31e795f21a0'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: '儲ける_8847608c-533c-4846-ae38-e31e795f21a0-1'}), (d:ClaimNode {nodeId: '8847608c-533c-4846-ae38-e31e795f21a0-1'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:SynonymNode {nodeId:'生む_8847608c-533c-4846-ae38-e31e795f21a0-1', nodeName:'生む', propositionId:'ceb278e8-ac27-4d44-9b63-00f091748e34', sentenceId:'8847608c-533c-4846-ae38-e31e795f21a0'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: '生む_8847608c-533c-4846-ae38-e31e795f21a0-1'}), (d:ClaimNode {nodeId: '8847608c-533c-4846-ae38-e31e795f21a0-1'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:SynonymNode {nodeId:'出産_8847608c-533c-4846-ae38-e31e795f21a0-1', nodeName:'出産', propositionId:'ceb278e8-ac27-4d44-9b63-00f091748e34', sentenceId:'8847608c-533c-4846-ae38-e31e795f21a0'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: '出産_8847608c-533c-4846-ae38-e31e795f21a0-1'}), (d:ClaimNode {nodeId: '8847608c-533c-4846-ae38-e31e795f21a0-1'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:SynonymNode {nodeId:'産み落とす_8847608c-533c-4846-ae38-e31e795f21a0-1', nodeName:'産み落とす', propositionId:'ceb278e8-ac27-4d44-9b63-00f091748e34', sentenceId:'8847608c-533c-4846-ae38-e31e795f21a0'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: '産み落とす_8847608c-533c-4846-ae38-e31e795f21a0-1'}), (d:ClaimNode {nodeId: '8847608c-533c-4846-ae38-e31e795f21a0-1'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:ClaimNode {nodeName: '案ずる', nodeId:'8847608c-533c-4846-ae38-e31e795f21a0-0', propositionId:'ceb278e8-ac27-4d44-9b63-00f091748e34', sentenceId:'8847608c-533c-4846-ae38-e31e795f21a0', currentId:'0', parentId:'1', isMainSection:'false', surface:'案ずるより', normalizedName:'案ずる', dependType:'D', caseType:'連用', namedEntity:'', rangeExpressions:'{"":{}}', categories:'{"":""}', domains:'{"":""}', knowledgeFeatureReferences:'[]', isDenialWord:'false',isConditionalConnection:'false',normalizedNameYomi:'あんずる',surfaceYomi:'あんずるより',modalityType:'-',logicType:'-',morphemes:'["動詞,*,ザ変動詞,基本形","助詞,接続助詞,*,*"]',lang:'ja_JP'})
          |MERGE (:SynonymNode {nodeId:'案じる_8847608c-533c-4846-ae38-e31e795f21a0-0', nodeName:'案じる', propositionId:'ceb278e8-ac27-4d44-9b63-00f091748e34', sentenceId:'8847608c-533c-4846-ae38-e31e795f21a0'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: '案じる_8847608c-533c-4846-ae38-e31e795f21a0-0'}), (d:ClaimNode {nodeId: '8847608c-533c-4846-ae38-e31e795f21a0-0'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |""".stripMargin

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
  /*
  "An access of getQueryResult for Japanese knowledge" should {
    "returns an appropriate response" in {
      registSingleClaim(KnowledgeForParser( UUID.random.toString, UUID.random.toString, Knowledge("案ずるより産むが易し。","ja_JP", "{}", false )))
      val fr = FakeRequest(POST, "/getQueryResult")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse("""{ "query":"MATCH (n) WHERE n.lang='ja_JP' RETURN n ", "target": "" }"""))
      val result= call(controller.getQueryResult(), fr)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      assert(!contentAsString(result).equals(""))
    }
  }
  */
  "An access of getQueryFormattedResult for Nodes of Japanese knowledge." should {
    "returns an appropriate response" in {
      val knowledgeSentenceSetForParser = KnowledgeSentenceSetForParser(
        premiseList = List.empty[KnowledgeForParser],
        premiseLogicRelation = List.empty[PropositionRelation],
        claimList = List(KnowledgeForParser( UUID.random.toString, UUID.random.toString, Knowledge("案ずるより産むが易し。","ja_JP", "{}", false ))),
        claimLogicRelation = List.empty[PropositionRelation])
      TestUtils.registerData(knowledgeSentenceSetForParser, transversalState, addVectorFlag = false, neo4JUtilsObject = neo4JUtils)
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

  "An access of getQueryFormattedResult for Edges of Japanese knowledge." should {
    "returns an appropriate response" in {
      val knowledgeSentenceSetForParser = KnowledgeSentenceSetForParser(
        premiseList = List.empty[KnowledgeForParser],
        premiseLogicRelation = List.empty[PropositionRelation],
        claimList = List(KnowledgeForParser( UUID.random.toString, UUID.random.toString, Knowledge("案ずるより産むが易し。","ja_JP", "{}", false ))),
        claimLogicRelation = List.empty[PropositionRelation])
      TestUtils.registerData(knowledgeSentenceSetForParser, transversalState, addVectorFlag = false, neo4JUtilsObject = neo4JUtils)
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

  "An access of getQueryFormattedResult for Synonym Nodes of Japanese knowledge." should {
    "returns an appropriate response" in {
      val knowledgeSentenceSetForParser = KnowledgeSentenceSetForParser(
        premiseList = List.empty[KnowledgeForParser],
        premiseLogicRelation = List.empty[PropositionRelation],
        claimList = List(KnowledgeForParser( UUID.random.toString, UUID.random.toString, Knowledge("彼はおにぎりを購入した。","ja_JP", "{}", false ))),
        claimLogicRelation = List.empty[PropositionRelation])
      TestUtils.registerData(knowledgeSentenceSetForParser, transversalState, addVectorFlag = false, neo4JUtilsObject = neo4JUtils)
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


  "An access of getQueryFormattedResult for Synonym Edges of Japanese knowledge." should {
    "returns an appropriate response" in {
      val knowledgeSentenceSetForParser = KnowledgeSentenceSetForParser(
        premiseList = List.empty[KnowledgeForParser],
        premiseLogicRelation = List.empty[PropositionRelation],
        claimList = List(KnowledgeForParser(UUID.random.toString, UUID.random.toString, Knowledge("彼はおにぎりを購入した。", "ja_JP", "{}", false))),
        claimLogicRelation = List.empty[PropositionRelation])
      TestUtils.registerData(knowledgeSentenceSetForParser, transversalState, addVectorFlag = false, neo4JUtilsObject = neo4JUtils)
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

  "An access of getQueryFormattedResult for Image Nodes of Japanese knowledge." should {
    "returns an appropriate response" in {
      val reference1 = Reference(url = "", surface = "猫が", surfaceIndex = 0, isWholeSentence = false, originalUrlOrReference = "http://images.cocodataset.org/val2017/000000039769.jpg")
      val referenceImage1 = ImageReference(reference = reference1, x = 0, y = 0, width = 128, height = 128)
      val featureId1 = UUID.random.toString
      val knowledgeForImage1 = KnowledgeForImage(featureId1, referenceImage1)
      val knowledgeSentenceSetForParser = KnowledgeSentenceSetForParser(
        premiseList = List.empty[KnowledgeForParser],
        premiseLogicRelation = List.empty[PropositionRelation],
        claimList = List(KnowledgeForParser(UUID.random.toString, UUID.random.toString, Knowledge("猫が２匹います。", "ja_JP", "{}", false, List(knowledgeForImage1)))),
        claimLogicRelation = List.empty[PropositionRelation])
      TestUtils.registerData(knowledgeSentenceSetForParser, transversalState, addVectorFlag = false, neo4JUtilsObject = neo4JUtils)
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

  "An access of getQueryFormattedResult for Image Edges of Japanese knowledge." should {
    "returns an appropriate response" in {
      val reference1 = Reference(url = "", surface = "猫が", surfaceIndex = 0, isWholeSentence = false, originalUrlOrReference = "http://images.cocodataset.org/val2017/000000039769.jpg")
      val referenceImage1 = ImageReference(reference = reference1, x = 0, y = 0, width = 128, height = 128)
      val featureId1 = UUID.random.toString
      val knowledgeForImage1 = KnowledgeForImage(featureId1, referenceImage1)
      val knowledgeSentenceSetForParser = KnowledgeSentenceSetForParser(
        premiseList = List.empty[KnowledgeForParser],
        premiseLogicRelation = List.empty[PropositionRelation],
        claimList = List(KnowledgeForParser(UUID.random.toString, UUID.random.toString, Knowledge("猫が２匹います。", "ja_JP", "{}", false, List(knowledgeForImage1)))),
        claimLogicRelation = List.empty[PropositionRelation])
      TestUtils.registerData(knowledgeSentenceSetForParser, transversalState, addVectorFlag = false, neo4JUtilsObject = neo4JUtils)
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

  "The Document-Node-Test." should {
    "returns an appropriate response" in {
      val knowledgeForDocument= KnowledgeForDocument(id = UUID.random.toString, filename = "Test.pdf", url = "http://example.com/Test.pdf", titleOfTopPage = "テストタイトル")
      val documentPageReference = DocumentPageReference(pageNo = -1, references = List.empty[String], tableOfContents = List.empty[String], headlines = List.empty[String])
      val knowledgeSentenceSetForParser = KnowledgeSentenceSetForParser(
        premiseList = List.empty[KnowledgeForParser],
        premiseLogicRelation = List.empty[PropositionRelation],
        claimList = List(KnowledgeForParser(UUID.random.toString, UUID.random.toString, Knowledge("これはドキュメント用のテストです。", "ja_JP", "{}", false, knowledgeForDocument=knowledgeForDocument, documentPageReference=documentPageReference))),
        claimLogicRelation = List.empty[PropositionRelation])
      TestUtils.registerData(knowledgeSentenceSetForParser, transversalState, addVectorFlag = false, neo4JUtilsObject = neo4JUtils)

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
