package com.heimdali.modules

import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.fs.FileSystem

trait FileSystemModule[F[_]] extends LazyLogging {
  this: ContextModule[F]
    with ConfigurationModule =>

  val hadoopFileSystem: FileSystem =
    FileSystem.get(hadoopConfiguration)
}
