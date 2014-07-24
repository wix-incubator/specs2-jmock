name := "specs2-jmock"

organization := "com.wixpress"

version := "0.1"

libraryDependencies ++= Seq(
  "org.specs2" % "specs2_2.10" % "2.3.12",
  "org.jmock" % "jmock-junit4" % "2.6.0",
  "org.jmock" % "jmock-legacy" % "2.6.0"
)

pomExtra :=
<licenses>
  <license>
    <name>LGPLv3</name>
    <url>https://www.gnu.org/licenses/lgpl.html</url>
    <distribution>repo</distribution>
  </license>
</licenses>