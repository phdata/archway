package com.heimdali.modules

import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.fs.FileSystem

trait FileSystemModule extends LazyLogging {
  this: ContextModule with ConfigurationModule =>

  val hadoopFileSystem: FileSystem =
    FileSystem.get(hadoopConfiguration)
}
