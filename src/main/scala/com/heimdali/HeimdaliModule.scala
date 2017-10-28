package com.heimdali

import be.objectify.deadbolt.scala.cache.HandlerCache
import com.google.inject.{AbstractModule, Provides}
import com.heimdali.actors._
import com.heimdali.repositories.{ProjectRepository, ProjectRepositoryImpl}
import com.heimdali.services._
import com.heimdali.startup._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.hdfs.client.HdfsAdmin
import play.api.libs.concurrent.AkkaGuiceSupport

class HeimdaliModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
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
    bind(classOf[DBMigration]).to(classOf[FlywayMigration])
    bind(classOf[SecurityContext]).to(classOf[UGISecurityContext])
    bind(classOf[LoginContextProvider]).to(classOf[UGILoginContextProvider])
    bind(classOf[KeytabService]).to(classOf[KeytabServiceImpl])
    bindActor[LDAPActor]("ldap-actor")
    bindActor[ProjectSaver]("project-saver")
    bindActor[HDFSActor]("hdfs-actor")
    bindActor[KeytabActor]("keytab-actor")
    bindActorFactory[ProjectProvisioner, ProjectProvisioner.Factory]
  }

  @Provides
  def provideAdmin(fileSystem: FileSystem, configuration: Configuration): HdfsAdmin = {
    new HdfsAdmin(fileSystem.getUri, configuration)
  }

}
