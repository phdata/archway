import sbt._
import sbt.Keys._

object Models {
  
  val modelsSettings = Seq(
     libraryDependencies ++= Dependencies.modelsDependencies
  )
  
}