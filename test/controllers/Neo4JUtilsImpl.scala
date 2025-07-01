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
