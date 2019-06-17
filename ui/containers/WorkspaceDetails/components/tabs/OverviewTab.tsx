import * as React from 'react';
import { Row, Col } from 'antd';
import { ApprovalCard, HDFSCard, HiveCard, PoolCard } from '../cards';
import { Workspace } from '../../../../models/Workspace';
import { Profile } from '../../../../models/Profile';

interface Props {
  workspace: Workspace;
  profile: Profile;
  approveRisk: (e: React.MouseEvent) => void;
  approveOperations: (e: React.MouseEvent) => void;
}

class OverviewTab extends React.Component<Props> {
  public render() {
    const { workspace, profile, approveRisk, approveOperations } = this.props;

    return (
      <div style={{ padding: 16 }}>
        <Row gutter={16} type="flex">
          <Col span={12}>
            <ApprovalCard
              data={workspace.approvals && workspace.approvals.risk}
              onApprove={profile.permissions && profile.permissions.risk_management ? approveRisk : undefined}
            >
              COMPLIANCE
            </ApprovalCard>
          </Col>
          <Col span={12}>
            <ApprovalCard
              data={workspace.approvals && workspace.approvals.infra}
              onApprove={profile.permissions && profile.permissions.platform_operations ? approveOperations : undefined}
            >
              PLATFORM OPERATIONS
            </ApprovalCard>
          </Col>
        </Row>
        <Row style={{ paddingTop: 16 }} gutter={16} type="flex">
          <Col span={8}>
            <HDFSCard data={workspace.data} />
          </Col>
          <Col span={8}>
            <HiveCard data={workspace.data} />
          </Col>
          <Col span={8}>
            <PoolCard data={workspace.processing} />
          </Col>
        </Row>
      </div>
    );
  }
}

export default OverviewTab;
