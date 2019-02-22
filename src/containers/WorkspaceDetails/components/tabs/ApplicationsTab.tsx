import * as React from 'react';
import { Row, Col } from 'antd';

import { ApplicationsInfoCard, YarnApplicationsCard } from '../cards';
import { YarnService } from '../../../../models/Cluster';
import { Workspace, Application, ResourcePoolsInfo } from '../../../../models/Workspace';

interface Props {
  workspace?: Workspace;
  yarn?: YarnService;
  pools?: ResourcePoolsInfo;
  onRefreshPools?: () => void;
}

const renderApplicationCard = (application: Application, index: number) => (
  <ApplicationsInfoCard key={index} application={application} />
);

class ApplicationsTab extends React.Component<Props> {
  public render() {
    const { workspace, yarn, pools, onRefreshPools } = this.props;
    const rmURL = yarn && `${yarn.resource_manager[0].host}:${yarn.resource_manager[0].port}`;

    return (
      <div style={{ padding: 16 }}>
        <Row style={{ minHeight: 'calc(100vh - 465px)' }} gutter={16} type="flex">
          <Col span={8}>
            {workspace && workspace.applications.length > 0
              ? workspace.applications.map(renderApplicationCard)
              : 'No applications.'}
          </Col>
          <Col span={16}>
            <YarnApplicationsCard
              pools={pools}
              rmURL={rmURL}
              onRefreshPools={onRefreshPools}
            />
          </Col>
        </Row>
      </div>
    );
  }
}

export default ApplicationsTab;
