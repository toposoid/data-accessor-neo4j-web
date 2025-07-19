import de.heikoseeberger.sbtheader.License
name := """data-accessor-neo4j-web"""
organization := "com.ideal.linked"
version := "0.6"

lazy val root = (project in file(".")).enablePlugins(PlayScala).enablePlugins(AutomateHeaderPlugin)

scalaVersion := "2.13.11"

libraryDependencies += guice
libraryDependencies += "com.ideal.linked" %% "scala-common" % "0.6"
libraryDependencies += "com.ideal.linked" %% "scala-data-accessor-neo4j" % "0.6"
libraryDependencies += "com.ideal.linked" %% "toposoid-knowledgebase-model" % "0.6"
libraryDependencies += "com.ideal.linked" %% "toposoid-deduction-protocol-model" % "0.6"
libraryDependencies += "com.ideal.linked" %% "toposoid-common" % "0.6"
libraryDependencies += "com.google.code.gson" % "gson" % "2.8.5"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
libraryDependencies += "com.ideal.linked" %% "toposoid-test-utils" % "0.6" % Test
libraryDependencies += "io.jvm.uuid" %% "scala-uuid" % "0.3.1" % Test

organizationName := "Linked Ideal LLC.[https://linked-ideal.com/]"
startYear := Some(2021)
licenses += ("AGPL-3.0-or-later", new URL("http://www.gnu.org/licenses/agpl-3.0.en.html"))
headerLicense := Some(License.AGPLv3("2025", organizationName.value))
