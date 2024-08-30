package controllers

import com.ideal.linked.data.accessor.neo4j.Neo4JAccessor
import com.ideal.linked.toposoid.common.TransversalState
import com.ideal.linked.toposoid.sentence.transformer.neo4j.Neo4JUtils

class Neo4JUtilsImpl extends Neo4JUtils {
  def executeQuery(query: String, transversalState: TransversalState): Unit = {
    Neo4JAccessor.executeQuery(query)
  }
}
