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

import com.google.gson.{Gson, GsonBuilder}
import com.ideal.linked.data.accessor.neo4j.Neo4JAccessor
import com.ideal.linked.toposoid.common.{CLAIM, PREMISE}
import com.ideal.linked.toposoid.knowledgebase.model.{KnowledgeBaseEdge, KnowledgeBaseNode, KnowledgeBaseSynonymEdge, KnowledgeBaseSynonymNode, KnowledgeFeatureReference, LocalContext, OtherElement, PredicateArgumentStructure}
import com.ideal.linked.toposoid.protocol.model.neo4j.{CypherQuery, Neo4jRecodeUnit, Neo4jRecordMap, Neo4jRecords}
import com.typesafe.scalalogging.LazyLogging
import org.neo4j.driver.{Record, Result}
import org.neo4j.driver.internal.value.{NodeValue, RelationshipValue, StringValue, ValueAdapter}

import javax.inject._
import play.api._
import play.api.libs.json.Json
import play.api.mvc._

import scala.jdk.CollectionConverters._


/**
 * This controller creates an `Action` to get　information from Neo4J graph database.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController with LazyLogging {

  /**
   * With json as input When a Cypher Query is requested, the result of executing the query is output as Json.
   * @return
   */
  @deprecated("Reason: There is a problem with deserialization")
  def getQueryResult()  = Action(parse.json) { request =>
    try {
      val json = request.body
      val cypherQuery:CypherQuery = Json.parse(json.toString).as[CypherQuery]
      logger.info(cypherQuery.query)
      val result:Result = Neo4JAccessor.executeQueryAndReturn(cypherQuery.query)
      val jsonStrBf = new StringBuilder
      val gson:Gson = new GsonBuilder().disableHtmlEscaping().create()
      var recordNo:Int = 0
      jsonStrBf.append("{\"records\":[")
      while (result.hasNext()) { //カラム方向のループ
        val record:Record = result.next()
        logger.debug(gson.toJson(record.fields()))
        if(recordNo == 0){jsonStrBf.append(gson.toJson(record.fields()))}
        else{jsonStrBf.append(","+gson.toJson(record.fields()))}
        recordNo += 1
      }
      jsonStrBf.append("]}")
      logger.debug(convert(jsonStrBf.toString()))
      Ok(convert(jsonStrBf.toString())).as(JSON)

    }catch{
      case e: Exception => {
        logger.error(e.toString, e)
        BadRequest(Json.obj("status" ->"Error", "message" -> e.toString()))
      }
    }
  }

  /**
   * With json as input When a Cypher Query is requested, the result of executing the query is output as Json.
   * @return
   */
  def getQueryFormattedResult()  = Action(parse.json) { request =>
    try {
      val json = request.body
      val cypherQuery:CypherQuery = Json.parse(json.toString).as[CypherQuery]
      logger.info(cypherQuery.query)
      val result:Result = Neo4JAccessor.executeQueryAndReturn(cypherQuery.query)
      var recordList:List[List[Neo4jRecordMap]] = List.empty[List[Neo4jRecordMap]]
      while (result.hasNext()) { //カラム方向のループ
        val record:Record = result.next()
        var recordMapList:List[Neo4jRecordMap] = List.empty[Neo4jRecordMap]

        val fields = record.fields.listIterator()
        while(fields.hasNext){
          val pair =  fields.next()
          val recordMap = new Neo4jRecordMap(pair.key, makeJsonPartialStr(pair.key, pair.value().asInstanceOf[ValueAdapter]))
          recordMapList = recordMapList :+ recordMap
        }
        recordList = recordList :+ recordMapList
      }
      val neo4jRecords:Neo4jRecords = new Neo4jRecords(recordList)
      Ok(Json.toJson(neo4jRecords)).as(JSON)

    }catch{
      case e: Exception => {
        logger.error(e.toString, e)
        BadRequest(Json.obj("status" ->"Error", "message" -> e.toString()))
      }
    }

  }

  /**
   * This function creates a Json string for output
   * @param key
   * @param value
   * @return
   */
  private def makeJsonPartialStr(key:String, value:ValueAdapter): Neo4jRecodeUnit ={


    val defaltLocalContext = new LocalContext(
        lang = "",
        namedEntity = "",
        rangeExpressions = Map.empty[String, Map[String, String]],
        categories = Map.empty[String,String],
        domains = Map.empty[String,String],
        knowledgeFeatureReferences = List.empty[KnowledgeFeatureReference]
    )

    val defaultPredicateArgumentStructure = new PredicateArgumentStructure(
        currentId = -99,
        parentId = -99,
        isMainSection = false,
        surface = "",
        normalizedName = "",
        dependType = "",
        caseType = "",
        isDenialWord = false,
        isConditionalConnection = false,
        normalizedNameYomi = "",
        surfaceYomi = "",
        modalityType = "",
        logicType = "",
        nodeType = -1,
        morphemes = List.empty[String]
    )

    val defaultLogicNode = new KnowledgeBaseNode(
        nodeId = "",
        propositionId = "",
        sentenceId = "",
        predicateArgumentStructure = defaultPredicateArgumentStructure,
        localContext = defaltLocalContext)

    val defaultLogicEdge = new KnowledgeBaseEdge("","", "",  "", "-", "")
    val defaultSynonymNode = new KnowledgeBaseSynonymNode("", "", "", "")
    val defaultSynonymEdge = new KnowledgeBaseSynonymEdge("", "", -1.0f)
    val defaultOtherElement = new OtherElement("")

    value match {
      case _ : NodeValue => {
        logger.debug("NodeValue")
        val node:NodeValue = value.asInstanceOf[NodeValue]
        if(node.asNode().hasLabel("PremiseNode") || node.asNode().hasLabel("ClaimNode")) {

          val nodeType:Int = node.asNode().hasLabel("PremiseNode") match {
            case true => PREMISE.index
            case _ => CLAIM.index
          }

          val localContext:LocalContext = new LocalContext(
            lang = node.get("lang").asString(),
            namedEntity = node.get("namedEntity").asString(),
            rangeExpressions = convertMapForRangeExpression(node.get("rangeExpressions").asString()),
            categories = convertMap(node.get("categories").asString()),
            domains = convertMap(node.get("domains").asString()),
            knowledgeFeatureReferences = convertList2JsonForKnowledgeFeatureReference(node.get("knowledgeFeatureReferences").asString())
          )

          val predicateArgumentStructure = new PredicateArgumentStructure(
            currentId = node.get("currentId").asString().toInt,
            parentId = node.get("parentId").asString().toInt,
            isMainSection = node.get("isMainSection").asString().toBoolean,
            surface = node.get("surface").asString(),
            normalizedName = node.get("normalizedName").asString(),
            dependType = node.get("dependType").asString(),
            caseType = node.get("caseType").asString(),
            isDenialWord = node.get("isDenialWord").asString().toBoolean,
            isConditionalConnection = node.get("isConditionalConnection").asString().toBoolean,
            normalizedNameYomi = node.get("normalizedNameYomi").asString(),
            surfaceYomi = node.get("surfaceYomi").asString(),
            modalityType = node.get("modalityType").asString(),
            logicType = node.get("logicType").asString(),
            nodeType = nodeType,
            morphemes = convertListMorphemes(node.get("morphemes").asString())
          )

          val logicNode:KnowledgeBaseNode = new KnowledgeBaseNode(
            nodeId = node.get("nodeId").asString(),
            propositionId = node.get("propositionId").asString(),
            sentenceId = node.get("sentenceId").asString(),
            predicateArgumentStructure = predicateArgumentStructure,
            localContext = localContext
          )

          new Neo4jRecodeUnit(logicNode, defaultLogicEdge, defaultSynonymNode, defaultSynonymEdge, defaultOtherElement)

        }else if(node.asNode().hasLabel("SynonymNode")){

          val synonymNode:KnowledgeBaseSynonymNode = new KnowledgeBaseSynonymNode(
            node.get("nodeId").asString(),
            node.get("nodeName").asString(),
            node.get("propositionId").asString(),
            node.get("sentenceId").toString
          )
          new Neo4jRecodeUnit(defaultLogicNode, defaultLogicEdge,  synonymNode, defaultSynonymEdge, defaultOtherElement)
        }else{
          new Neo4jRecodeUnit(defaultLogicNode, defaultLogicEdge, defaultSynonymNode, defaultSynonymEdge, defaultOtherElement)
        }
      }
      case _ : RelationshipValue => {
        //sourceId:String, destinationId:String, caseStr:String, dependType:String
        logger.debug("RelationshipValue")
        val link:RelationshipValue = value.asInstanceOf[RelationshipValue]
        if(link.asRelationship().hasType("PremiseEdge") || link.asRelationship().hasType("ClaimEdge")){

          val start = link.asRelationship().startNodeId()
          val end = link.asRelationship().endNodeId()
          val logicEdge = new KnowledgeBaseEdge(
            link.get("sourceId").asString(),
            link.get("destinationId").asString(),
            link.get("caseName").asString(),
            link.get("dependType").asString(),
            link.get("logicType").asString(),
            link.get("lang").asString()
          )
          new Neo4jRecodeUnit(defaultLogicNode, logicEdge, defaultSynonymNode, defaultSynonymEdge, defaultOtherElement)

        }else if(link.asRelationship().hasType("SynonymEdge")){
          val synonymEdge = new KnowledgeBaseSynonymEdge(
            link.get("sourceId").asString(),
            link.get("destinationId").asString(),
            link.get("similarity").asFloat()
          )
          new Neo4jRecodeUnit(defaultLogicNode, defaultLogicEdge, defaultSynonymNode, synonymEdge, defaultOtherElement)
        }else{
          new Neo4jRecodeUnit(defaultLogicNode, defaultLogicEdge, defaultSynonymNode, defaultSynonymEdge, defaultOtherElement)
        }
      }
      case _ : StringValue => {
        logger.debug("StringValue")
        val element:StringValue = value.asInstanceOf[StringValue]
        val otherElement = new OtherElement(element.asString())
        new Neo4jRecodeUnit(defaultLogicNode, defaultLogicEdge, defaultSynonymNode, defaultSynonymEdge, otherElement)

      }
      case _ => {
        logger.warn("Not Match")
        new Neo4jRecodeUnit(defaultLogicNode, defaultLogicEdge, defaultSynonymNode, defaultSynonymEdge, defaultOtherElement)
      }
    }
  }

  /**
   * Deserialize the Json string
   * @param s
   * @return
   */
  private def convertMap(s:String):Map[String,String] ={
    Json.parse(s).as[Map[String,String]]
  }

  /**
   * Deserialize the Json string
   * @param l
   * @return
   */
  private def convertList2JsonForKnowledgeFeatureReference(s:String): List[KnowledgeFeatureReference] = {
    Json.parse(s).as[List[KnowledgeFeatureReference]]
  }

  /**
   * Deserialize the Json string for the RangeExpression
   * @param s
   * @return
   */
  private def convertMapForRangeExpression(s:String):Map[String, Map[String, String]] = {
    Json.parse(s).as[Map[String, Map[String, String]]]
  }

  private def convertListMorphemes(s:String):List[String] = {
    Json.parse(s).as[List[String]]
  }
  /**
   * Formatting Json
   * @param s
   * @return
   */
  def convert(s:String): String = {
    val idx = s.indexOf("{\"val\":")
    if(idx == -1) return s
    val convertStr:String = s.substring(0, idx) + s.substring(idx + 7).replaceFirst("\"},\"", "\",\"")
    if(convertStr.indexOf("{\"val\":") != -1) convert(convertStr)
    else convertStr.replaceAll("}}}}}", "}}}}")
  }

}
