import * as React from 'react';
import { Row, Col } from 'antd';

import { ManageCard } from '../cards';
import { ProvisioningType, ModalType } from '../../../../constants';

interface Props {
  provisioning: ProvisioningType;
  showModal: (e: React.MouseEvent, type: ModalType) => void;
}

const ManageTab = ({ provisioning, showModal }: Props) => (
  <div style={{ padding: 16, fontSize: 17 }}>
    <Row gutter={16} type="flex">
      <Col span={8}>
        <ManageCard
          title="PROVISION"
          showModal={showModal}
          modalType={ModalType.ProvisionWorkspace}
          buttonText="Provision"
          disabled={provisioning === ProvisioningType.Pending}
        >
          Provisioning a workspace manually is not necessary under normal circumstances. Automatic provisioning
          scheduled and will automatically re-attempt errored workspaces. This manual provisioning can be used to
          reprovision a workspace that has already successfully provisioned.
        </ManageCard>
      </Col>
      <Col span={8}>
        <ManageCard
          title="DELETE"
          showModal={showModal}
          modalType={ModalType.DeleteWorkspace}
          buttonText="Delete Workspace"
          disabled={false}
        >
          Deleting a workspace will delete the record of the workspace from Heimdali only. Deleting a workspace will not
          affect the Hive database, AD groups, Sentry roles, or any other components of the workspace. Deleting a
          workspace cannot be undone.
        </ManageCard>
      </Col>
      <Col span={8}>
        <ManageCard
          title="DEPROVISION"
          showModal={showModal}
          modalType={ModalType.DeprovisionWorkspace}
          buttonText="Deprovision"
          disabled={provisioning === ProvisioningType.Pending}
        >
          Deprovisioning a workspace will attempt to remove components of the workspace including Hive databases, AD
          groups, and Sentry roles. Deleting a workspace will not remove any data on HDFS.
        </ManageCard>
      </Col>
    </Row>
  </div>
);

export default ManageTab;
