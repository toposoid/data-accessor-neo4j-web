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
import play.api.test.Helpers.{POST, contentType, defaultAwaitTimeout, status, _}
import play.api.test.{FakeRequest, _}
import io.jvm.uuid.UUID
class HomeControllerSpecEnglish extends PlaySpec with BeforeAndAfter with BeforeAndAfterAll with GuiceOneAppPerSuite  with Injecting with LazyLogging {

  val transversalState: TransversalState = TransversalState(userId = "test-user", username = "guest", roleId = 0, csrfToken = "")
  val transversalStateJson: String = Json.toJson(transversalState).toString()
  val neo4JUtils = new Neo4JUtilsImpl()
  /*
  def registSingleClaim(knowledgeForParser: KnowledgeForParser): Unit = {
    val asos = TestUtils.getAnalyzedSentenceObjects(knowledgeForParser, transversalState)
    val analyzedPropositionPair: AnalyzedPropositionPair = AnalyzedPropositionPair(asos, knowledgeForParser)
    val analyzedPropositionSet = AnalyzedPropositionSet(
      List.empty[AnalyzedPropositionPair],
      List.empty[PropositionRelation],
      List(analyzedPropositionPair),
      List.empty[PropositionRelation])
    Sentence2Neo4jTransformer.createGraph(analyzedPropositionSet, transversalState, neo4JUtils)
  }
  */
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

  "An access of executeQuery for registering English knowledge" should {
    "returns an appropriate response" in {

      val query: String ="""
          |MERGE (:ClaimNode {nodeName: 'time', nodeId:'5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-0', propositionId:'6fbc8f56-7aa6-4519-b667-61a8a40060d2', sentenceId:'5b5c6277-d63b-4ce8-9f3d-c05b5e485f89', currentId:'0', parentId:'1', isMainSection:'true', surface:'Time', normalizedName:'time', dependType:'-', caseType:'nsubj', namedEntity:'', rangeExpressions:'{"":{}}', categories:'{}', domains:'{}', knowledgeFeatureReferences:'[]', isDenialWord:'false',isConditionalConnection:'false',normalizedNameYomi:'',surfaceYomi:'',modalityType:'-',logicType:'-',morphemes:'["NOUN"]',lang:'en_US'})
          |MERGE (:SynonymNode {nodeId:'time_5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-0', nodeName:'time', propositionId:'6fbc8f56-7aa6-4519-b667-61a8a40060d2', sentenceId:'5b5c6277-d63b-4ce8-9f3d-c05b5e485f89'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: 'time_5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-0'}), (d:ClaimNode {nodeId: '5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-0'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:ClaimNode {nodeName: 'be', nodeId:'5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-1', propositionId:'6fbc8f56-7aa6-4519-b667-61a8a40060d2', sentenceId:'5b5c6277-d63b-4ce8-9f3d-c05b5e485f89', currentId:'1', parentId:'1', isMainSection:'true', surface:'is', normalizedName:'be', dependType:'-', caseType:'ROOT', namedEntity:'', rangeExpressions:'{"":{}}', categories:'{}', domains:'{}', knowledgeFeatureReferences:'[]', isDenialWord:'false',isConditionalConnection:'false',normalizedNameYomi:'',surfaceYomi:'',modalityType:'-',logicType:'-',morphemes:'["AUX"]',lang:'en_US'})
          |MERGE (:SynonymNode {nodeId:'Be_5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-1', nodeName:'Be', propositionId:'6fbc8f56-7aa6-4519-b667-61a8a40060d2', sentenceId:'5b5c6277-d63b-4ce8-9f3d-c05b5e485f89'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: 'Be_5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-1'}), (d:ClaimNode {nodeId: '5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-1'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:SynonymNode {nodeId:'exist_5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-1', nodeName:'exist', propositionId:'6fbc8f56-7aa6-4519-b667-61a8a40060d2', sentenceId:'5b5c6277-d63b-4ce8-9f3d-c05b5e485f89'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: 'exist_5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-1'}), (d:ClaimNode {nodeId: '5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-1'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:SynonymNode {nodeId:'be_5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-1', nodeName:'be', propositionId:'6fbc8f56-7aa6-4519-b667-61a8a40060d2', sentenceId:'5b5c6277-d63b-4ce8-9f3d-c05b5e485f89'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: 'be_5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-1'}), (d:ClaimNode {nodeId: '5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-1'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:SynonymNode {nodeId:'represent_5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-1', nodeName:'represent', propositionId:'6fbc8f56-7aa6-4519-b667-61a8a40060d2', sentenceId:'5b5c6277-d63b-4ce8-9f3d-c05b5e485f89'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: 'represent_5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-1'}), (d:ClaimNode {nodeId: '5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-1'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:ClaimNode {nodeName: 'money', nodeId:'5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-2', propositionId:'6fbc8f56-7aa6-4519-b667-61a8a40060d2', sentenceId:'5b5c6277-d63b-4ce8-9f3d-c05b5e485f89', currentId:'2', parentId:'1', isMainSection:'true', surface:'money', normalizedName:'money', dependType:'-', caseType:'attr', namedEntity:'', rangeExpressions:'{"":{}}', categories:'{}', domains:'{}', knowledgeFeatureReferences:'[]', isDenialWord:'false',isConditionalConnection:'false',normalizedNameYomi:'',surfaceYomi:'',modalityType:'-',logicType:'-',morphemes:'["NOUN"]',lang:'en_US'})
          |MERGE (:SynonymNode {nodeId:'money_5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-2', nodeName:'money', propositionId:'6fbc8f56-7aa6-4519-b667-61a8a40060d2', sentenceId:'5b5c6277-d63b-4ce8-9f3d-c05b5e485f89'})
          |UNION ALL
          |MATCH (s:SynonymNode {nodeId: 'money_5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-2'}), (d:ClaimNode {nodeId: '5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-2'}) MERGE (s)-[:SynonymEdge {similarity:0.5}]->(d)
          |UNION ALL
          |MERGE (:ClaimNode {nodeName: '.', nodeId:'5b5c6277-d63b-4ce8-9f3d-c05b5e485f89-3', propositionId:'6fbc8f56-7aa6-4519-b667-61a8a40060d2', sentenceId:'5b5c6277-d63b-4ce8-9f3d-c05b5e485f89', currentId:'3', parentId:'1', isMainSection:'true', surface:'.', normalizedName:'.', dependType:'-', caseType:'punct', namedEntity:'', rangeExpressions:'{"":{}}', categories:'{}', domains:'{}', knowledgeFeatureReferences:'[]', isDenialWord:'false',isConditionalConnection:'false',normalizedNameYomi:'',surfaceYomi:'',modalityType:'-',logicType:'-',morphemes:'["PUNCT"]',lang:'en_US'})
          """.stripMargin

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
  "An access of getQueryResult for English knowledge" should {
    "returns an appropriate response" in {
      registSingleClaim(KnowledgeForParser( UUID.random.toString, UUID.random.toString, Knowledge("Time is money.","en_US", "{}", false )))
      val fr = FakeRequest(POST, "/getQueryResult")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse("""{ "query":"MATCH (n) WHERE n.lang='en_US' RETURN n ", "target": "" }"""))
      val result= call(controller.getQueryResult(), fr)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      assert(!contentAsString(result).equals(""))
    }
  }
  */
  "An access of getQueryFormattedResult for English knowledge Nodes" should {
    "returns an appropriate response" in {
      val knowledgeSentenceSetForParser = KnowledgeSentenceSetForParser(
        premiseList = List.empty[KnowledgeForParser],
        premiseLogicRelation = List.empty[PropositionRelation],
        claimList = List(KnowledgeForParser( UUID.random.toString, UUID.random.toString, Knowledge("Time is money.","en_US", "{}", false ))),
        claimLogicRelation = List.empty[PropositionRelation])
      TestUtils.registerData(knowledgeSentenceSetForParser, transversalState, addVectorFlag = false, neo4JUtilsObject = neo4JUtils)
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

  "An access of getQueryFormattedResult2 for English knowledge Nodes" should {
    "returns an appropriate response" in {
      val knowledgeSentenceSetForParser = KnowledgeSentenceSetForParser(
        premiseList = List.empty[KnowledgeForParser],
        premiseLogicRelation = List.empty[PropositionRelation],
        claimList = List(KnowledgeForParser(UUID.random.toString, UUID.random.toString, Knowledge("That's life.", "en_US", "{}", false))),
        claimLogicRelation = List.empty[PropositionRelation])
      TestUtils.registerData(knowledgeSentenceSetForParser, transversalState, addVectorFlag = false, neo4JUtilsObject = neo4JUtils)
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
        .withJsonBody(Json.parse("""{ "query":"MATCH (n) WHERE n.lang='en_US' RETURN n", "target": "" }"""))
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


  "An access of getQueryFormattedResult for English knowledge Edges" should {
    "returns an appropriate response" in {
      val knowledgeSentenceSetForParser = KnowledgeSentenceSetForParser(
        premiseList = List.empty[KnowledgeForParser],
        premiseLogicRelation = List.empty[PropositionRelation],
        claimList = List(KnowledgeForParser( UUID.random.toString, UUID.random.toString, Knowledge("Time is money.","en_US", "{}", false ))),
        claimLogicRelation = List.empty[PropositionRelation])
      TestUtils.registerData(knowledgeSentenceSetForParser, transversalState, addVectorFlag = false, neo4JUtilsObject = neo4JUtils)
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

  "An access of getQueryFormattedResult for Synonym Nodes of English knowledge." should {
    "returns an appropriate response" in {
      val knowledgeSentenceSetForParser = KnowledgeSentenceSetForParser(
        premiseList = List.empty[KnowledgeForParser],
        premiseLogicRelation = List.empty[PropositionRelation],
        claimList = List(KnowledgeForParser(UUID.random.toString, UUID.random.toString, Knowledge("He has a good chance.", "en_US", "{}", false))),
        claimLogicRelation = List.empty[PropositionRelation])
      TestUtils.registerData(knowledgeSentenceSetForParser, transversalState, addVectorFlag = false, neo4JUtilsObject = neo4JUtils)
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

  "An access of getQueryFormattedResult for Synonym Edges of English knowledge." should {
    "returns an appropriate response" in {
      val knowledgeSentenceSetForParser = KnowledgeSentenceSetForParser(
        premiseList = List.empty[KnowledgeForParser],
        premiseLogicRelation = List.empty[PropositionRelation],
        claimList = List(KnowledgeForParser(UUID.random.toString, UUID.random.toString, Knowledge("He has a good chance.", "en_US", "{}", false))),
        claimLogicRelation = List.empty[PropositionRelation])
      TestUtils.registerData(knowledgeSentenceSetForParser, transversalState, addVectorFlag = false, neo4JUtilsObject = neo4JUtils)

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

    "An access of getQueryFormattedResult for Image Nodes of English knowledge." should {
      "returns an appropriate response" in {
        val reference1 = Reference(url = "", surface = "cats", surfaceIndex = 3, isWholeSentence = false, originalUrlOrReference = "http://images.cocodataset.org/val2017/000000039769.jpg")
        val referenceImage1 = ImageReference(reference = reference1, x = 0, y = 0, width = 128, height = 128)
        val featureId1 = UUID.random.toString
        val knowledgeForImage1 = KnowledgeForImage(featureId1, referenceImage1)
        val knowledgeSentenceSetForParser = KnowledgeSentenceSetForParser(
          premiseList = List.empty[KnowledgeForParser],
          premiseLogicRelation = List.empty[PropositionRelation],
          claimList = List(KnowledgeForParser(UUID.random.toString, UUID.random.toString, Knowledge("There are two cats.", "en_US", "{}", false, List(knowledgeForImage1)))),
          claimLogicRelation = List.empty[PropositionRelation])
        TestUtils.registerData(knowledgeSentenceSetForParser, transversalState, addVectorFlag = false, neo4JUtilsObject = neo4JUtils)
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

    "An access of getQueryFormattedResult for Image Edges of Japanese knowledge." should {
      "returns an appropriate response" in {
        val reference1 = Reference(url = "", surface = "cats", surfaceIndex = 3, isWholeSentence = false, originalUrlOrReference = "http://images.cocodataset.org/val2017/000000039769.jpg")
        val referenceImage1 = ImageReference(reference = reference1, x = 0, y = 0, width = 128, height = 128)
        val featureId1 = UUID.random.toString
        val knowledgeForImage1 = KnowledgeForImage(featureId1, referenceImage1)
        val knowledgeSentenceSetForParser = KnowledgeSentenceSetForParser(
          premiseList = List.empty[KnowledgeForParser],
          premiseLogicRelation = List.empty[PropositionRelation],
          claimList = List(KnowledgeForParser(UUID.random.toString, UUID.random.toString, Knowledge("There are two cats.", "en_US", "{}", false, List(knowledgeForImage1)))),
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

    "The Document-Node-Test." should {
      "returns an appropriate response" in {
        val knowledgeForDocument = KnowledgeForDocument(id = UUID.random.toString, filename = "Test.pdf", url = "http://example.com/Test.pdf", titleOfTopPage = "TestTitle")
        val documentPageReference = DocumentPageReference(pageNo = -1, references = List.empty[String], tableOfContents = List.empty[String], headlines = List.empty[String])
        val knowledgeSentenceSetForParser = KnowledgeSentenceSetForParser(
          premiseList = List.empty[KnowledgeForParser],
          premiseLogicRelation = List.empty[PropositionRelation],
          claimList = List(KnowledgeForParser(UUID.random.toString, UUID.random.toString, Knowledge("This is a documentation test.", "en_US", "{}", false, knowledgeForDocument = knowledgeForDocument, documentPageReference = documentPageReference))),
          claimLogicRelation = List.empty[PropositionRelation])
        TestUtils.registerData(knowledgeSentenceSetForParser, transversalState, addVectorFlag = false, neo4JUtilsObject = neo4JUtils)

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
}
