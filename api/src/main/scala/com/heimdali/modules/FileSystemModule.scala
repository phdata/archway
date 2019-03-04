package com.heimdali.modules

import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.fs.FileSystem

trait FileSystemModule[F[_]] extends LazyLogging {
  this: ConfigurationModule =>

  val hadoopFileSystem: FileSystem =
    FileSystem.get(hadoopConfiguration)
}
