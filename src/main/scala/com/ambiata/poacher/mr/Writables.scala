package com.ambiata.poacher.mr

import org.apache.hadoop.io.BytesWritable

object Writables {

  def bytesWritable(capacity: Int): BytesWritable = {
    val bw = new BytesWritable
    bw.setCapacity(capacity)
    bw
  }
}
