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

lazy val verifyNotSnapshot = TaskKey[Unit]("verifyNotSnapshot", "Checks that the version derived by DynVer plugin is not snapshot")
verifyNotSnapshot := {
  val v = version.value
  if (dynverGitDescribeOutput.value.hasNoTags)
    throw new MessageOnlyException(
      s"Failed to derive version from git tags. Maybe run `git fetch --unshallow`? Version: $v"
    )
  if (dynverGitDescribeOutput.value.isSnapshot) {
    throw new MessageOnlyException(
      s"Snapshot publishing disabled. Version: $v"
    )
  }
}

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % "4.8.3",
  "org.specs2" %% "specs2-mock" % "4.8.3",
  "org.jmock" % "jmock" % "2.12.0",
  "org.jmock" % "jmock-junit4" % "2.12.0",
  "org.jmock" % "jmock-imposters" % "2.12.0",
  "org.hamcrest" % "hamcrest" % "2.2",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.12.3",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.12.3",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.12.3" % Test,
)
crossScalaVersions := Seq("2.12.10", "2.11.12", "2.13.6")
scalaVersion := "2.13.6"
Test / fork := true
Test / scalacOptions ++= Seq("-Yrangepos")
publishTo := Some(Resolver.file("sonatype-local-bundle", sonatypeBundleDirectory.value))

