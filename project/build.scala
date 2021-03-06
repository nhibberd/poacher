import sbt._
import Keys._
import sbt.KeyRanks._
import sbtassembly.Plugin._
import AssemblyKeys._
import com.ambiata.promulgate.project.ProjectPlugin._
import scoverage.ScoverageSbtPlugin._

object build extends Build {
  type Settings = Def.Setting[_]

  lazy val hadoopVersion =
    Option(System.getenv("HADOOP_VERSION")).getOrElse("yarn")

  lazy val ossBucket: String =
    sys.env.getOrElse("AMBIATA_IVY_OSS", "ambiata-oss")

  lazy val poacher = Project(
    id = "poacher"
  , base = file(".")
  , settings =
    standardSettings ++
    promulgate.library(s"com.ambiata.poacher", ossBucket) ++
    Seq[Settings](libraryDependencies ++= depend.scalaz ++ depend.mundane ++ depend.scoobi(hadoopVersion) ++ depend.hadoop(hadoopVersion) ++ depend.specs2 ++ depend.thrift ++ depend.shapeless ++ depend.disorder)
  )

  lazy val standardSettings = Defaults.coreDefaultSettings ++
                              projectSettings              ++
                              compilationSettings          ++
                              testingSettings              ++
                              Seq[Settings](
                                resolvers := depend.resolvers
                              )

  lazy val projectSettings: Seq[Settings] = Seq(
    name := "poacher"
  , version in ThisBuild := s"""1.0.0-$hadoopVersion"""
  , unmanagedSourceDirectories in Compile += (sourceDirectory in Compile).value / s"scala-$hadoopVersion"
  , organization := "com.ambiata"
  , scalaVersion := "2.11.2"
  , crossScalaVersions := Seq("2.11.2")
  , fork in run  := true
  , publishArtifact in (Test, packageBin) := true
  )

  lazy val compilationSettings: Seq[Settings] = Seq(
    javaOptions ++= Seq("-Xmx3G", "-Xms512m", "-Xss4m")
    , javacOptions ++= Seq("-source", "1.6", "-target", "1.6")
    , maxErrors := 10
    ,  scalacOptions in Compile ++= Seq(
        "-target:jvm-1.6"
      , "-deprecation"
      , "-unchecked"
      , "-feature"
      , "-language:_"
      , "-Ywarn-value-discard"
      , "-Ywarn-unused-import"
      , "-Yno-adapted-args"
      , "-Xlint"
      , "-Xfatal-warnings"
      , "-Yinline-warnings")
    , scalacOptions in (Compile,doc) := Seq("-language:_", "-feature")
  )

  lazy val testingSettings: Seq[Settings] = Seq(
    initialCommands in console := "import org.specs2._"
  , logBuffered := false
  , cancelable := true
  , fork in Test := true
  , javaOptions += "-Xmx3G"
  , testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "tracefilter", "/.*specs2.*,.*mundane.testing.*")
  )

  lazy val buildAssemblySettings: Seq[Settings] = Seq(
    artifact in (Compile, assembly) ~= { art =>
      art.copy(`classifier` = Some("assembly"))
    }
  ) ++ addArtifact(artifact in (Compile, assembly), assembly)
}
