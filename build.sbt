name := "specs2-jmock"

description := "A specs2 JMock integration layer, that provides a nice DSL, especially targeted at people used to JMock in Java and Junit."

organization := "com.wixpress"

version := "0.1"

crossScalaVersions := Seq("2.10.4", "2.11.4")

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.3.12",
  "org.jmock" % "jmock-junit4" % "2.6.0",
  "org.jmock" % "jmock-legacy" % "2.6.0"
)

publishTo := Some("Wix Repository" at "http://repo.dev.wix/artifactory/libs-snapshots-local")

credentials += Credentials(Path.userHome / ".m2" / ".creds")

publishMavenStyle := true

pomExtra :=
<licenses>
  <license>
    <name>LGPLv3</name>
    <url>https://www.gnu.org/licenses/lgpl.html</url>
    <distribution>repo</distribution>
  </license>
</licenses>
<developers>
  <developer>
    <name>Nimrod Argov</name>
    <email>nimroda@wix.com</email>
    <organization>Wix</organization>
    <roles>
      <role>owner</role>
    </roles>
  </developer>
</developers>

releaseSettings
