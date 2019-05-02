package com.heimdali.generators

import java.time.Instant

import cats.effect.{Clock, Sync}
import cats.implicits._
import com.heimdali.config.AppConfig
import com.heimdali.models._

class DefaultUserWorkspaceGenerator[F[_]](appConfig: AppConfig,
                                          ldapGroupGenerator: LDAPGroupGenerator[F],
                                          applicationGenerator: ApplicationGenerator[F],
                                          topicGenerator: TopicGenerator[F])
                                         (implicit val clock: Clock[F], val F: Sync[F])
  extends WorkspaceGenerator[F, UserTemplate] {

  override def defaults(user: User): F[UserTemplate] =
    F.pure(
      UserTemplate(user.distinguishedName, user.username, Some(appConfig.workspaces.user.defaultSize), Some(appConfig.workspaces.sharedWorkspace.defaultCores), Some(appConfig.workspaces.sharedWorkspace.defaultMemory))
    )

  override def workspaceFor(template: UserTemplate): F[WorkspaceRequest] = {
    for {
      time <- clock.realTime(scala.concurrent.duration.MILLISECONDS)

      generatedName = WorkspaceGenerator.generateName(template.username)

      workspace = WorkspaceRequest(
        template.username,
        template.username,
        template.username,
        "user",
        template.userDN,
        Instant.ofEpochMilli(time),
        Compliance(phiData = false, pciData = false, piiData = false),
        singleUser = true,
        processing = List(Yarn(
          s"${appConfig.workspaces.user.poolParents}.$generatedName",
          appConfig.workspaces.user.defaultCores,
          appConfig.workspaces.user.defaultMemory)))

      managerHive <- ldapGroupGenerator
        .generate(
          s"user_$generatedName",
          s"cn=user_$generatedName,${appConfig.ldap.groupPath}",
          s"role_user_$generatedName",
          workspace)
      topic <- topicGenerator.topicFor("default", 1, 1, workspace)
    } yield workspace.copy(
      data = List(HiveAllocation(
        s"user_$generatedName",
        s"${appConfig.workspaces.user.root}/$generatedName/db",
        appConfig.workspaces.user.defaultSize,
        managerHive,
        None,
        None
      )),
      kafkaTopics = List(topic)
    )
  }

}
