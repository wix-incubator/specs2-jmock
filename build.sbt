import sbt.Keys._

inThisBuild(List(
  name := "specs2-jmock",
  description := "A specs2 JMock integration layer that provides a nice DSL, especially targeted at people used to JMock in Java and Junit.",
  organization := "com.wix",
  homepage := Some(url("https://github.com/wix/specs2-jmock")),
  licenses := Seq("BSD-style" → url("http://www.opensource.org/licenses/bsd-license.php")),
  developers := List(Developer(
    "pijusn",
    "Pijus Navickas",
    "pijusn@wix.com",
    url("https://github.com/pijusn")
  ))
))

publish / skip := true

publishTo := sonatypePublishToBundle.value

lazy val specs2JMock = project
  .withId("specs2-jmock")
  .in(file("."))
  .settings(
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2-core" % "4.3.1",
      "org.specs2" %% "specs2-mock" % "4.3.1",
      "org.jmock" % "jmock" % "2.12.0",
      "org.jmock" % "jmock-junit4" % "2.12.0",
      "org.jmock" % "jmock-imposters" % "2.12.0",
      "org.hamcrest" % "hamcrest" % "2.1"
    ),
    crossScalaVersions := Seq("2.12.10", "2.11.12"),
    scalaVersion := "2.12.10",
    Test / scalacOptions ++= Seq("-Yrangepos")
  )


//crossScalaVersions := Seq("2.12.10", "2.11.12")
//
//scalaVersion := "2.12.10"
//
//libraryDependencies ++= Seq(
//  "org.specs2" %% "specs2-core" % "4.3.1",
//  "org.specs2" %% "specs2-mock" % "4.3.1",
//  "org.jmock" % "jmock" % "2.12.0",
//  "org.jmock" % "jmock-junit4" % "2.12.0",
//  "org.jmock" % "jmock-imposters" % "2.12.0",
//  "org.hamcrest" % "hamcrest" % "2.1"
//)
//
//Test / scalacOptions ++= Seq("-Yrangepos")

//publishArtifact in Test := false

//pomIncludeRepository := { _ ⇒ false }
//
//pomExtra :=
//<scm>
//  <url>git@github.com:wix/specs2-jmock.git</url>
//  <connection>scm:git:git@github.com:wix/specs2-jmock.git</connection>
//</scm>
//<developers>
//  <developer>
//    <id>pijusn</id>
//    <name>Pijus Navickas</name>
//    <email>pijusn@wix.com</email>
//    <organization>Wix</organization>
//    <roles>
//      <role>owner</role>
//    </roles>
//  </developer>
//</developers>
