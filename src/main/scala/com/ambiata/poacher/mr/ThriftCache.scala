package com.ambiata.poacher.mr

import com.ambiata.poacher.hdfs._

import scalaz._

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.mapreduce.Job

/**
 * This is module for managing passing thrift data-types via the distributed cache. This is
 * _unsafe_ at best, and should be used with extreme caution. The only valid reason to
 * use it is when writing raw map reduce jobs.
 */
case class ThriftCache(base: HdfsPath, id: ContextId) {
  val distCache = DistCache(base, id)
  val serializer = ThriftSerialiser()

  /** Push a thrift data-type to the distributed cache for this job, under the
     specified key. This fails _hard_ if anything goes wrong. */
  def push[A](job: Job, key: ThriftCache.Key, a: A)(implicit ev: A <:< ThriftLike): Unit = {
    val bytes = serializer.toByteViewUnsafe(a)
    distCache.pushView(job, DistCache.Key(key.value), bytes.bytes, bytes.length)
  }

  /** Pop a thrift data-type from the distributed job, it is assumed that this is
     only run by map or reduce tasks where to the cache for this job where a call
     to ThriftCache#push has prepared everything. This fails _hard_ if anything
     goes wrong. NOTE: argument is updated, rather than a new value returned. */
  def pop[A](conf: Configuration, key: ThriftCache.Key, a: A)(implicit ev: A <:< ThriftLike): Unit = {
    distCache.pop(conf, DistCache.Key(key.value), bytes =>
      \/.fromTryCatchNonFatal(serializer.fromBytesUnsafe(a, bytes)).leftMap(_.toString))
    ()
  }
}

object ThriftCache {
  case class Key(value: String)
}
