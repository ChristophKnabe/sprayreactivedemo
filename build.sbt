//Describes how to build a demo of reactiveness using spray-can client API. Knabe 2014-06-24
import com.github.retronym.SbtOneJar._

organization in ThisBuild := "de.bht-berlin.knabe"

name := "sprayreactivedemo-SNAPSHOT"

version := "1.0"

scalaVersion  := "2.10.3"

scalacOptions := Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= Seq(
  "io.spray" % "spray-can" % "1.3.1",
  //"io.spray" % "spray-httpx" % "1.3.1",
  "com.typesafe.akka" %% "akka-actor" % "2.3.0",
  "org.scalatest" %% "scalatest" % "2.1.6" % "test",
  "junit" % "junit" % "4.11" % "test"
)

//Set main class for sbt-run command:
mainClass in (Compile,run) := Some("crawl.Main")

//Set main class for packaging:
mainClass in (Compile,packageBin) <<= mainClass in (Compile,run)

//For using the SBT One-Jar plugin:
oneJarSettings

