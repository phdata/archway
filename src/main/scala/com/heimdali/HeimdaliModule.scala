package com.heimdali

import akka.routing.RoundRobinPool
import be.objectify.deadbolt.scala.cache.HandlerCache
import com.google.inject.matcher.Matchers
import com.google.inject.spi.ProvisionListener
import com.google.inject.{AbstractModule, Provides}
import com.heimdali.actors.{HDFSActor, LDAPActor, ProjectProvisioner, ProjectSaver}
import com.heimdali.repositories.{ProjectRepository, ProjectRepositoryImpl}
import com.heimdali.services._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.hdfs.client.HdfsAdmin
import play.api.Logger
import play.api.libs.concurrent.AkkaGuiceSupport

class HeimdaliModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindListener(Matchers.any, new ProvisionListener() {
      override def onProvision[A](provision: ProvisionListener.ProvisionInvocation[A]): Unit = {
        Logger.warn(s"Provisioning: ${provision.getBinding}")
      }
    })
    bind(classOf[HandlerCache]).to(classOf[HeimdaliCache])
    bind(classOf[LDAPClient]).to(classOf[LDAPClientImpl])
    bind(classOf[AccountService]).to(classOf[LDAPAccountService])
    bind(classOf[ProjectService]).to(classOf[ProjectServiceImpl])
    bind(classOf[ProjectRepository]).to(classOf[ProjectRepositoryImpl])
    bind(classOf[ClusterService]).to(classOf[CDHClusterService])
    bind(classOf[Configuration]).toInstance(new Configuration())
    bind(classOf[HDFSClient]).to(classOf[HDFSClientImpl])
    bind(classOf[FileSystem]).toInstance(FileSystem.get(new Configuration()))
    bind(classOf[Startup]).to(classOf[HeimdaliStartup]).asEagerSingleton()
    bindActor[LDAPActor]("ldap-actor")
    bindActor[ProjectSaver]("project-saver")
    bindActor[HDFSActor]("hdfs-actor")
    bindActor[ProjectProvisioner]("provisioning-actor", RoundRobinPool(5).props)
  }

  @Provides
  def provideAdmin(fileSystem: FileSystem, configuration: Configuration): HdfsAdmin = {
    new HdfsAdmin(fileSystem.getUri, configuration)
  }

}
