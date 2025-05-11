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

import com.ideal.linked.toposoid.common.{CLAIM, IMAGE, PREMISE, TABLE}
import com.ideal.linked.toposoid.knowledgebase.model.{KnowledgeBaseEdge, KnowledgeBaseGlobalNode, KnowledgeBaseNode, KnowledgeBaseSemiGlobalEdge, KnowledgeBaseSemiGlobalNode, KnowledgeBaseSynonymEdge, KnowledgeBaseSynonymNode, KnowledgeFeatureReference, KnowledgeFeatureReferenceEdge, LocalContext, LocalContextForFeature, OtherElement, PredicateArgumentStructure}
import com.ideal.linked.toposoid.protocol.model.neo4j.Neo4jRecodeUnit
import org.neo4j.driver.internal.value.{NodeValue, RelationshipValue, StringValue, ValueAdapter}
import play.api.libs.json.Json

object Neo4jRecordUtils {
  /**
   * This function creates a Json string for output
   *
   * @param key
   * @param value
   * @return
   */
  def makeJsonPartialStr(key: String, value: ValueAdapter): Neo4jRecodeUnit = {

    val defaultLogicNode = None
    val defaultLogicEdge = None
    val defaultSemiGlobalNode = None
    val defaultSemiGlobalEdge = None
    val defaultGlobalNode = None
    val defaultGlobalEdge = None
    val defaultSynonymNode = None
    val defaultSynonymEdge = None
    val defaultKnowledgeFeatureReference = None
    val defaultKnowledgeFeatureReferenceEdge = None
    val defaultOtherElement = None

    value match {
      case _: NodeValue => {

        val node: NodeValue = value.asInstanceOf[NodeValue]
        if (node.asNode().hasLabel("PremiseNode") || node.asNode().hasLabel("ClaimNode")) {

          val nodeType: Int = node.asNode().hasLabel("PremiseNode") match {
            case true => PREMISE.index
            case _ => CLAIM.index
          }

          val localContext: LocalContext = new LocalContext(
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
            parallelType = node.get("parallelType").asString(),
            nodeType = nodeType,
            morphemes = convertListMorphemes(node.get("morphemes").asString())
          )

          val logicNode: KnowledgeBaseNode = new KnowledgeBaseNode(
            nodeId = node.get("nodeId").asString(),
            propositionId = node.get("propositionId").asString(),
            sentenceId = node.get("sentenceId").asString(),
            predicateArgumentStructure = predicateArgumentStructure,
            localContext = localContext
          )

          new Neo4jRecodeUnit(Option(logicNode), defaultLogicEdge, defaultSemiGlobalNode, defaultSemiGlobalEdge, defaultGlobalNode, defaultGlobalEdge, defaultSynonymNode, defaultSynonymEdge, defaultKnowledgeFeatureReference, defaultKnowledgeFeatureReferenceEdge, defaultOtherElement)
        } else if (node.asNode().hasLabel("SemiGlobalPremiseNode") || node.asNode().hasLabel("SemiGlobalClaimNode")) {

          val sentenceType: Int = node.asNode().hasLabel("SemiGlobalPremiseNode") match {
            case true => PREMISE.index
            case _ => CLAIM.index
          }

          val localContextForFeature: LocalContextForFeature = new LocalContextForFeature(
            lang = node.get("lang").asString(),
            knowledgeFeatureReferences = convertList2JsonForKnowledgeFeatureReference(node.get("knowledgeFeatureReferences").asString())
          )

          val semiGlobalNode: KnowledgeBaseSemiGlobalNode = new KnowledgeBaseSemiGlobalNode(
            node.get("sentenceId").asString(),
            node.get("propositionId").asString(),
            node.get("documentId").asString(),
            node.get("sentence").asString(),
            sentenceType,
            localContextForFeature,
          )
          new Neo4jRecodeUnit(defaultLogicNode, defaultLogicEdge, Option(semiGlobalNode), defaultSemiGlobalEdge, defaultGlobalNode, defaultGlobalEdge, defaultSynonymNode, defaultSynonymEdge, defaultKnowledgeFeatureReference, defaultKnowledgeFeatureReferenceEdge, defaultOtherElement)
        } else if (node.asNode().hasLabel("GlobalNode")) {
          val globalNode = KnowledgeBaseGlobalNode(
            documentId = node.get("documentId").asString(),
            filename = node.get("filename").asString(),
            url = node.get("url").asString(),
            titleOfTopPage = node.get("url").asString(),
            totalPageNum = node.get("totalPageNum").asString().toInt
          )
          new Neo4jRecodeUnit(defaultLogicNode, defaultLogicEdge, defaultSemiGlobalNode, defaultSemiGlobalEdge, Option(globalNode), defaultGlobalEdge, defaultSynonymNode, defaultSynonymEdge, defaultKnowledgeFeatureReference, defaultKnowledgeFeatureReferenceEdge, defaultOtherElement)
        } else if (node.asNode().hasLabel("SynonymNode")) {

          val synonymNode: KnowledgeBaseSynonymNode = new KnowledgeBaseSynonymNode(
            node.get("nodeId").asString(),
            node.get("nodeName").asString(),
            node.get("propositionId").asString(),
            node.get("sentenceId").asString()
          )
          new Neo4jRecodeUnit(defaultLogicNode, defaultLogicEdge, defaultSemiGlobalNode, defaultSemiGlobalEdge, defaultGlobalNode, defaultGlobalEdge, Option(synonymNode), defaultSynonymEdge, defaultKnowledgeFeatureReference, defaultKnowledgeFeatureReferenceEdge, defaultOtherElement)
        } else if (node.asNode().hasLabel("ImageNode")) {

          val imageNode: KnowledgeFeatureReference = new KnowledgeFeatureReference(
            propositionId = node.get("propositionId").asString(),
            sentenceId = node.get("sentenceId").asString(),
            featureId = node.get("featureId").asString(),
            featureType = IMAGE.index,
            url = node.get("url").asString(),
            source = node.get("source").asString()
          )
          new Neo4jRecodeUnit(defaultLogicNode, defaultLogicEdge, defaultSemiGlobalNode, defaultSemiGlobalEdge, defaultGlobalNode, defaultGlobalEdge, defaultSynonymNode, defaultSynonymEdge, Option(imageNode), defaultKnowledgeFeatureReferenceEdge, defaultOtherElement)
        } else if (node.asNode().hasLabel("TableNode")) {
          val tableNode: KnowledgeFeatureReference = new KnowledgeFeatureReference(
            propositionId = node.get("propositionId").asString(),
            sentenceId = node.get("sentenceId").asString(),
            featureId = node.get("featureId").asString(),
            featureType = TABLE.index,
            url = node.get("url").asString(),
            source = node.get("source").asString()
          )
          new Neo4jRecodeUnit(defaultLogicNode, defaultLogicEdge, defaultSemiGlobalNode, defaultSemiGlobalEdge, defaultGlobalNode, defaultGlobalEdge, defaultSynonymNode, defaultSynonymEdge, Option(tableNode), defaultKnowledgeFeatureReferenceEdge, defaultOtherElement)
        } else {
          new Neo4jRecodeUnit(defaultLogicNode, defaultLogicEdge, defaultSemiGlobalNode, defaultSemiGlobalEdge, defaultGlobalNode, defaultGlobalEdge, defaultSynonymNode, defaultSynonymEdge, defaultKnowledgeFeatureReference, defaultKnowledgeFeatureReferenceEdge, defaultOtherElement)
        }
      }
      case _: RelationshipValue => {
        val link: RelationshipValue = value.asInstanceOf[RelationshipValue]
        if (link.asRelationship().hasType("LocalEdge")) {

          val localEdge = new KnowledgeBaseEdge(
            link.asRelationship().startNodeId().toString,
            link.asRelationship().endNodeId().toString,
            link.get("caseName").asString(),
            link.get("dependType").asString(),
            link.get("parallelType").asString(),
            link.get("hasInclusion").asString().toBoolean,
            link.get("logicType").asString()
          )
          new Neo4jRecodeUnit(defaultLogicNode, Option(localEdge), defaultSemiGlobalNode, defaultSemiGlobalEdge, defaultGlobalNode, defaultGlobalEdge, defaultSynonymNode, defaultSynonymEdge, defaultKnowledgeFeatureReference, defaultKnowledgeFeatureReferenceEdge, defaultOtherElement)
        } else if (link.asRelationship().hasType("SemiGlobalEdge")) {

          val semiGlobalEdge: KnowledgeBaseSemiGlobalEdge = new KnowledgeBaseSemiGlobalEdge(
            link.asRelationship().startNodeId().toString,
            link.asRelationship().endNodeId().toString,
            link.get("logicType").asString(),
          )
          new Neo4jRecodeUnit(defaultLogicNode, defaultLogicEdge, defaultSemiGlobalNode, Option(semiGlobalEdge), defaultGlobalNode, defaultGlobalEdge, defaultSynonymNode, defaultSynonymEdge, defaultKnowledgeFeatureReference, defaultKnowledgeFeatureReferenceEdge, defaultOtherElement)

        } else if (link.asRelationship().hasType("SynonymEdge")) {
          val synonymEdge = new KnowledgeBaseSynonymEdge(
            link.asRelationship().startNodeId().toString,
            link.asRelationship().endNodeId().toString,
            link.get("similarity").asFloat()
          )
          new Neo4jRecodeUnit(defaultLogicNode, defaultLogicEdge, defaultSemiGlobalNode, defaultSemiGlobalEdge, defaultGlobalNode, defaultGlobalEdge, defaultSynonymNode, Option(synonymEdge), defaultKnowledgeFeatureReference, defaultKnowledgeFeatureReferenceEdge, defaultOtherElement)
        } else if (link.asRelationship().hasType("ImageEdge")) {
          val imageEdge = new KnowledgeFeatureReferenceEdge(
            link.asRelationship().startNodeId().toString,
            link.asRelationship().endNodeId().toString
          )
          new Neo4jRecodeUnit(defaultLogicNode, defaultLogicEdge, defaultSemiGlobalNode, defaultSemiGlobalEdge, defaultGlobalNode, defaultGlobalEdge, defaultSynonymNode, defaultSynonymEdge, defaultKnowledgeFeatureReference, Option(imageEdge), defaultOtherElement)
        } else if (link.asRelationship().hasType("TableEdge")) {
          val tableEdge = new KnowledgeFeatureReferenceEdge(
            link.asRelationship().startNodeId().toString,
            link.asRelationship().endNodeId().toString
          )
          new Neo4jRecodeUnit(defaultLogicNode, defaultLogicEdge, defaultSemiGlobalNode, defaultSemiGlobalEdge, defaultGlobalNode, defaultGlobalEdge, defaultSynonymNode, defaultSynonymEdge, defaultKnowledgeFeatureReference, Option(tableEdge), defaultOtherElement)
        }
        else {
          new Neo4jRecodeUnit(defaultLogicNode, defaultLogicEdge, defaultSemiGlobalNode, defaultSemiGlobalEdge, defaultGlobalNode, defaultGlobalEdge, defaultSynonymNode, defaultSynonymEdge, defaultKnowledgeFeatureReference, defaultKnowledgeFeatureReferenceEdge, defaultOtherElement)
        }
      }
      case _: StringValue => {

        val element: StringValue = value.asInstanceOf[StringValue]
        val otherElement = new OtherElement(element.asString())
        new Neo4jRecodeUnit(defaultLogicNode, defaultLogicEdge, defaultSemiGlobalNode, defaultSemiGlobalEdge, defaultGlobalNode, defaultGlobalEdge, defaultSynonymNode, defaultSynonymEdge, defaultKnowledgeFeatureReference, defaultKnowledgeFeatureReferenceEdge, Option(otherElement))

      }
      case _ => {

        new Neo4jRecodeUnit(defaultLogicNode, defaultLogicEdge, defaultSemiGlobalNode, defaultSemiGlobalEdge, defaultGlobalNode, defaultGlobalEdge, defaultSynonymNode, defaultSynonymEdge, defaultKnowledgeFeatureReference, defaultKnowledgeFeatureReferenceEdge, defaultOtherElement)
      }
    }
  }

  /**
   * Deserialize the Json string
   *
   * @param s
   * @return
   */
  private def convertMap(s: String): Map[String, String] = {
    Json.parse(s).as[Map[String, String]]
  }

  /**
   * Deserialize the Json string
   *
   * @param l
   * @return
   */
  private def convertList2JsonForKnowledgeFeatureReference(s: String): List[KnowledgeFeatureReference] = {
    Json.parse(s).as[List[KnowledgeFeatureReference]]
  }

  /**
   * Deserialize the Json string for the RangeExpression
   *
   * @param s
   * @return
   */
  private def convertMapForRangeExpression(s: String): Map[String, Map[String, String]] = {
    Json.parse(s).as[Map[String, Map[String, String]]]
  }

  private def convertListMorphemes(s: String): List[String] = {
    Json.parse(s).as[List[String]]
  }

  /**
   * Formatting Json
   *
   * @param s
   * @return
   */
  def convert(s: String): String = {
    val idx = s.indexOf("{\"val\":")
    if (idx == -1) return s
    val convertStr: String = s.substring(0, idx) + s.substring(idx + 7).replaceFirst("\"},\"", "\",\"")
    if (convertStr.indexOf("{\"val\":") != -1) convert(convertStr)
    else convertStr.replaceAll("}}}}}", "}}}}")
  }
}
