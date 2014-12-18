package com.ambiata.poacher.hdfs

import com.ambiata.mundane.control._
import com.ambiata.mundane.io._
import org.apache.hadoop.conf.Configuration
import com.nicta.scoobi.Scoobi._
import scalaz._, effect.IO

object TemporaryConfiguration {
  def withConf[A](f: Configuration => ResultTIO[A]): ResultTIO[A] = TemporaryDirPath.withDirPath { dir =>
    runWithConf(dir, f)
  }

  def runWithConf[A](dir: DirPath, f: Configuration => ResultTIO[A]): ResultTIO[A] =
    runWithScoobiConf(dir, x => f(x))

  def runWithScoobiConf[A](dir: DirPath, f: ScoobiConfiguration => ResultTIO[A]): ResultTIO[A] = {
    val sc = ScoobiConfiguration()
    sc.set("hadoop.tmp.dir", dir.path)
    sc.set("scoobi.dir", dir.path + "/")
    f(sc)
  }

  def withConfX[A](f: Configuration => A): ResultTIO[A] = TemporaryDirPath.withDirPath { dir =>
    withConf(conf => ResultT.ok[IO, A](f(conf)))
  }
}