import * as React from 'react';
import { Row, Col } from 'antd';

import { WorkspaceCard, DeprovisionCard } from '../cards';
import { ProvisioningType } from '../../../../constants';

interface Props {
  provisioning: ProvisioningType;
  onDeleteWorkspace: (e: React.MouseEvent) => void;
  onDeprovisionWorkspace: (e: React.MouseEvent) => void;
}

const ManageTab = ({ provisioning, onDeleteWorkspace }: Props) => (
  <div style={{ padding: 16, fontSize: 17 }}>
    <Row gutter={16} type="flex">
      <Col span={12}>
        <WorkspaceCard onDeleteWorkspace={onDeleteWorkspace}>DELETE</WorkspaceCard>
      </Col>
      <Col span={12}>
        <DeprovisionCard onDeprovisionWorkspace={onDeleteWorkspace} provisioning={provisioning}>
          DEPROVISION
        </DeprovisionCard>
      </Col>
    </Row>
  </div>
);

export default ManageTab;
