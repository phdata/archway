import * as React from 'react';
import { Button, Row, Col, Tabs } from 'antd';

import { ApplicationCard, YarnResourceCard, YarnApplicationsCard } from '../cards';
import { YarnService } from '../../../../models/Cluster';
import { Workspace, Application, ResourcePoolsInfo } from '../../../../models/Workspace';
import { ModalType } from '../../../../constants';

interface Props {
  workspace?: Workspace;
  yarn?: YarnService;
  pools?: ResourcePoolsInfo;
  showModal: (e: React.MouseEvent, type: ModalType) => void;
  onRefreshPools?: () => void;
  onSelectApplication: (application: Application) => void;
}

const ApplicationsTab = ({ workspace, yarn, pools, showModal, onRefreshPools, onSelectApplication }: Props) => {
  const rmURL = yarn && `${yarn.resource_manager[0].host}:${yarn.resource_manager[0].port}`;

  return (
    <div style={{ padding: 12 }}>
      <Button
        type="primary"
        style={{ zIndex: 999, position: 'absolute', right: 12 }}
        onClick={e => showModal(e, ModalType.Application)}
      >
        Add an Application
      </Button>
      <Tabs onChange={activeKey => onSelectApplication(!!workspace && workspace.applications[activeKey])}>
        {!!workspace &&
          workspace.applications.map((application, index) => (
            <Tabs.TabPane tab={application.name} key={index.toString()}>
              <Row gutter={16} type="flex" justify="center">
                <Col key="application" span={12} style={{ marginBottom: 12 }}>
                  <ApplicationCard application={application} />
                </Col>
                <Col key="resource" span={12} style={{ marginBottom: 12 }}>
                  <YarnResourceCard resource={workspace.processing[0]} showModal={showModal} />
                </Col>
              </Row>
              <Row gutter={16} type="flex" justify="center">
                <Col span={24}>
                  <YarnApplicationsCard pools={pools} rmURL={rmURL} onRefreshPools={onRefreshPools} />
                </Col>
              </Row>
            </Tabs.TabPane>
          ))}
      </Tabs>
    </div>
  );
};

export default ApplicationsTab;
