import * as React from 'react';
import { Button, Row, Col, Tabs } from 'antd';

import { ApplicationCard, YarnResourceCard, YarnApplicationsCard } from '../cards';
import { YarnService } from '../../../../models/Cluster';
import { Workspace, Application, ResourcePoolsInfo } from '../../../../models/Workspace';

interface Props {
  workspace?: Workspace;
  yarn?: YarnService;
  pools?: ResourcePoolsInfo;
  selectedApplication?: Application;
  onAddApplication?: (e: React.MouseEvent) => void;
  onRefreshPools?: () => void;
  onSelectApplication: (application: Application) => void;
}

const ApplicationsTab = ({ workspace, selectedApplication, yarn, pools, onAddApplication, onRefreshPools }: Props) => {
  const rmURL = yarn && `${yarn.resource_manager[0].host}:${yarn.resource_manager[0].port}`;

  return (
    <div style={{ padding: 12 }}>
      <Button type="primary" style={{ zIndex: 999, position: 'absolute', right: 12 }} onClick={onAddApplication}>
        Add an Application
      </Button>
      <Tabs>
        {!!workspace && workspace.applications.map((application) => (
          <Tabs.TabPane tab={application.name} key={application.id.toString()}>
            <Row gutter={16} type="flex" justify="center">
              <Col key="application" span={12} style={{ marginBottom: 12 }}>
                <ApplicationCard application={application} />
              </Col>
              <Col key="resource" span={12} style={{ marginBottom: 12 }}>
                <YarnResourceCard resource={workspace.processing[0]} />
              </Col>
            </Row>
            <Row gutter={16} type="flex" justify="center">
              <Col span={24}>
                <YarnApplicationsCard
                  pools={pools}
                  rmURL={rmURL}
                  onRefreshPools={onRefreshPools}
                />
              </Col>
            </Row>
          </Tabs.TabPane>
        ))}
      </Tabs>
    </div>
  );
};

export default ApplicationsTab;
