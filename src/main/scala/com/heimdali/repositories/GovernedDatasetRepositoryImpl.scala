package com.heimdali.repositories

import com.heimdali.models._
import scalikejdbc._

import scala.concurrent.{ExecutionContext, Future}

class GovernedDatasetRepositoryImpl(implicit executionContext: ExecutionContext) extends GovernedDatasetRepository {

  override def find(names: Seq[String]): Future[Seq[GovernedDataset]] = Future {
    try {
      NamedDB('default) readOnly { implicit session =>
        val gd = GovernedDataset.syntax
        val c = Compliance.syntax

        val rd = Dataset.syntax("rd")
        val rdl = LDAPRegistration.syntax("rdl")
        val rdh = HiveDatabase.syntax("rdh")
        val rdy = Yarn.syntax("rdy")

        val sd = Dataset.syntax("sd")
        val sdl = LDAPRegistration.syntax("sdl")
        val sdh = HiveDatabase.syntax("sdh")
        val sdy = Yarn.syntax("sdy")

        val md = Dataset.syntax("md")
        val mdl = LDAPRegistration.syntax("mdl")
        val mdh = HiveDatabase.syntax("mdh")
        val mdy = Yarn.syntax("mdy")
        withSQL {
          select
            .from(GovernedDataset as gd)

            .innerJoin(Compliance as c).on(gd.complianceId, c.id)

            .innerJoin(Dataset as rd).on(gd.rawDatasetId, rd.id)
            .leftJoin(LDAPRegistration as rdl).on(rd.ldapRegistrationId, rdl.id)
            .leftJoin(HiveDatabase as rdh).on(rd.hiveDatabaseId, rdh.id)
            .leftJoin(Yarn as rdy).on(rd.yarnId, rdy.id)

            .innerJoin(Dataset as sd).on(gd.stagingDatasetId, sd.id)
            .leftJoin(LDAPRegistration as sdl).on(sd.ldapRegistrationId, sdl.id)
            .leftJoin(HiveDatabase as sdh).on(sd.hiveDatabaseId, sdh.id)
            .leftJoin(Yarn as sdy).on(sd.yarnId, sdy.id)

            .innerJoin(Dataset as md).on(gd.modeledDatasetId, md.id)
            .leftJoin(LDAPRegistration as mdl).on(md.ldapRegistrationId, mdl.id)
            .leftJoin(HiveDatabase as mdh).on(md.hiveDatabaseId, mdh.id)
            .leftJoin(Yarn as mdy).on(md.yarnId, mdy.id)

            .where.in(gd.systemName, names)
        }
          .map(GovernedDataset(gd.resultName,
            c.resultName,
            rd.resultName,
            rdl.resultName,
            rdh.resultName,
            rdy.resultName,
            sd.resultName,
            sdl.resultName,
            sdh.resultName,
            sdy.resultName,
            md.resultName,
            mdl.resultName,
            mdh.resultName,
            mdy.resultName))
          .list().apply()
      }
    } catch {
      case exc: Throwable =>
        exc.printStackTrace()
        throw exc
    }
  }

  override def create(governedDataset: GovernedDataset): Future[GovernedDataset] = Future {
    NamedDB('default) localTx { implicit session =>
      val id = applyUpdateAndReturnGeneratedKey {
        val d = GovernedDataset.column
        insert.into(GovernedDataset)
          .namedValues(
            d.name -> governedDataset.name,
            d.systemName -> governedDataset.systemName,
            d.purpose -> governedDataset.purpose,
            d.complianceId -> governedDataset.complianceId,
            d.rawDatasetId -> governedDataset.rawDatasetId,
            d.stagingDatasetId -> governedDataset.stagingDatasetId,
            d.modeledDatasetId -> governedDataset.modeledDatasetId,
            d.created -> governedDataset.created,
            d.createdBy -> governedDataset.createdBy,
            d.requestedSize -> governedDataset.requestedSize,
            d.requestedCores -> governedDataset.requestedCores,
            d.requestedMemory -> governedDataset.requestedMemory
          )
      }

      governedDataset.copy(id = Some(id))
    }
  }
}

