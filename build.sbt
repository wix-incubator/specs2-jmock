releaseSettings

name := "specs2-jmock"

description := "A specs2 JMock integration layer that provides a nice DSL, especially targeted at people used to JMock in Java and Junit."

organization := "com.wixpress"

crossScalaVersions := Seq("2.10.5", "2.11.6")

scalaVersion := "2.11.6"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % "3.5",
  "org.specs2" %% "specs2-mock" % "3.5",
  "org.jmock" % "jmock-junit4" % "2.6.0",
  "org.jmock" % "jmock-legacy" % "2.6.0",
  "org.hamcrest" % "hamcrest-core" % "1.3",
  "org.hamcrest" % "hamcrest-library" % "1.3"
)

scalacOptions in Test ++= Seq("-Yrangepos")

publishTo := {
  val wixRepo = "http://repo.dev.wix/artifactory/libs-"
  if(isSnapshot.value)
    Some("Wix Repo Snapshots" at wixRepo + "snapshots-local")
  else
    Some("Wix Repo Releases" at wixRepo + "releases-local")
}

credentials += Credentials(Path.userHome / ".m2" / ".creds")

publishMavenStyle := true

publishArtifact in Test := false

ReleaseKeys.publishArtifactsAction := PgpKeys.publishSigned.value

licenses := Seq("BSD-style" → url("http://www.opensource.org/licenses/bsd-license.php"))

pomExtra :=
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
