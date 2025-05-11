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
import com.ideal.linked.toposoid.common.{Neo4JUtils, ToposoidUtils, TransversalState}
import com.ideal.linked.toposoid.protocol.model.neo4j.{Neo4jRecordMap, Neo4jRecords}
import org.neo4j.driver.internal.value.ValueAdapter
import org.neo4j.driver.{Record, Result}
import play.api.libs.json.Json

class Neo4JUtilsImpl extends Neo4JUtils {
  def executeQuery(query: String, transversalState: TransversalState): Unit = {
    Neo4JAccessor.executeQuery(query)
  }

  override def executeQueryAndReturn(query: String, transversalState: TransversalState): Neo4jRecords = {

    val result: Result = Neo4JAccessor.executeQueryAndReturn(ToposoidUtils.decodeJsonInJson(query))
    var recordList: List[List[Neo4jRecordMap]] = List.empty[List[Neo4jRecordMap]]
    while (result.hasNext()) { //カラム方向のループ
      val record: Record = result.next()
      var recordMapList: List[Neo4jRecordMap] = List.empty[Neo4jRecordMap]

      val fields = record.fields.listIterator()
      while (fields.hasNext) {
        val pair = fields.next()
        val recordMap = Neo4jRecordMap(key = pair.key, Neo4jRecordUtils.makeJsonPartialStr(pair.key, value = pair.value().asInstanceOf[ValueAdapter]))
        recordMapList = recordMapList :+ recordMap
      }
      recordList = recordList :+ recordMapList
    }
    new Neo4jRecords(recordList)

  }
}
