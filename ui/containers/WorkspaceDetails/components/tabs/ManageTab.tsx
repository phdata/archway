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
          showModal={showModal}
          modalType={ModalType.ProvisionWorkspace}
          buttonText="Provision"
          disabled={provisioning === ProvisioningType.Pending}
        >
          PROVISION
        </ManageCard>
      </Col>
      <Col span={8}>
        <ManageCard
          showModal={showModal}
          modalType={ModalType.DeleteWorkspace}
          buttonText="Delete Workspace"
          disabled={false}
        >
          DELETE
        </ManageCard>
      </Col>
      <Col span={8}>
        <ManageCard
          showModal={showModal}
          modalType={ModalType.DeprovisionWorkspace}
          buttonText="Deprovision"
          disabled={provisioning === ProvisioningType.Pending}
        >
          DEPROVISION
        </ManageCard>
      </Col>
    </Row>
  </div>
);

export default ManageTab;
