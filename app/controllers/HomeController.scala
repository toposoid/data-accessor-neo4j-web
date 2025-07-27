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

import com.google.gson.{Gson, GsonBuilder}
import com.ideal.linked.data.accessor.neo4j.Neo4JAccessor
import com.ideal.linked.toposoid.common.{CLAIM, IMAGE, PREMISE, TABLE, TRANSVERSAL_STATE, ToposoidUtils, TransversalState}
import com.ideal.linked.toposoid.knowledgebase.model.{KnowledgeBaseEdge, KnowledgeBaseGlobalNode, KnowledgeBaseNode, KnowledgeBaseSemiGlobalEdge, KnowledgeBaseSemiGlobalNode, KnowledgeBaseSynonymEdge, KnowledgeBaseSynonymNode, KnowledgeFeatureReference, KnowledgeFeatureReferenceEdge, LocalContext, LocalContextForFeature, OtherElement, PredicateArgumentStructure}
import com.ideal.linked.toposoid.protocol.model.neo4j.{CypherQuery, Neo4jRecodeUnit, Neo4jRecordMap, Neo4jRecords}
import com.typesafe.scalalogging.LazyLogging
import org.neo4j.driver.{Record, Result}
import org.neo4j.driver.internal.value.{NodeValue, RelationshipValue, StringValue, ValueAdapter}

import javax.inject._
import play.api._
import play.api.libs.json.{Json, OWrites, Reads}
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
  /*
  @deprecated("Reason: There is a problem with deserialization")
  def getQueryResult()  = Action(parse.json) { request =>
    val transversalState = Json.parse(request.headers.get(TRANSVERSAL_STATE .str).get).as[TransversalState]
    try {
      val json = request.body
      val cypherQuery:CypherQuery = Json.parse(json.toString).as[CypherQuery]
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
      logger.debug(cypherQuery.query)
      logger.debug(convert(jsonStrBf.toString()))
      logger.info(ToposoidUtils.formatMessageForLogger("Issuing a query to Neo4j completed.", transversalState.userId))
      Ok(convert(jsonStrBf.toString())).as(JSON)

    }catch{
      case e: Exception => {
        logger.error(ToposoidUtils.formatMessageForLogger(e.toString, transversalState.userId), e)
        BadRequest(Json.obj("status" ->"Error", "message" -> e.toString()))
      }
    }
  }
  */
  /**
   *
   * @return
   */
  def executeQuery()= Action(parse.json) { request =>
    val transversalState = Json.parse(request.headers.get(TRANSVERSAL_STATE.str).get).as[TransversalState]
    try {
      val json = request.body
      val cypherQuery: CypherQuery = Json.parse(json.toString).as[CypherQuery]
      Neo4JAccessor.executeQuery(ToposoidUtils.decodeJsonInJson(cypherQuery.query))
      Ok(Json.obj("status" ->"OK", "message" -> ""))
    } catch {
      case e: Exception => {
        logger.error(ToposoidUtils.formatMessageForLogger(e.toString, transversalState.userId), e)
        BadRequest(Json.obj("status" -> "Error", "message" -> e.toString()))
      }
    }
  }
    /**
   * With json as input When a Cypher Query is requested, the result of executing the query is output as Json.
   * @return
   */
  def getQueryFormattedResult()  = Action(parse.json) { request =>
    val transversalState = Json.parse(request.headers.get(TRANSVERSAL_STATE .str).get).as[TransversalState]
    try {
      val json = request.body
      val cypherQuery:CypherQuery = Json.parse(json.toString).as[CypherQuery]
      val result:Result = Neo4JAccessor.executeQueryAndReturn(ToposoidUtils.decodeJsonInJson(cypherQuery.query))
      var recordList:List[List[Neo4jRecordMap]] = List.empty[List[Neo4jRecordMap]]
      while (result.hasNext()) { //カラム方向のループ
        val record:Record = result.next()
        var recordMapList:List[Neo4jRecordMap] = List.empty[Neo4jRecordMap]

        val fields = record.fields.listIterator()
        while(fields.hasNext){
          val pair =  fields.next()
          val recordMap = Neo4jRecordMap(key = pair.key, Neo4jRecordUtils.makeJsonPartialStr(pair.key, value = pair.value().asInstanceOf[ValueAdapter]))
          recordMapList = recordMapList :+ recordMap
        }
        recordList = recordList :+ recordMapList
      }
      val neo4jRecords:Neo4jRecords = new Neo4jRecords(recordList)
      logger.debug(cypherQuery.query)
      logger.info(ToposoidUtils.formatMessageForLogger("Issuing a query to Neo4j completed.", transversalState.userId))
      Ok(Json.toJson(neo4jRecords)).as(JSON)

    }catch{
      case e: Exception => {
        logger.error(ToposoidUtils.formatMessageForLogger(e.toString, transversalState.userId), e)
        BadRequest(Json.obj("status" ->"Error", "message" -> e.toString()))
      }
    }

  }





}
