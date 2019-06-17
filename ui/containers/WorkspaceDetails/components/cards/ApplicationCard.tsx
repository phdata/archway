import * as React from 'react';
import { Card, Row, Col, Icon } from 'antd';

import { Application } from '../../../../models/Workspace';

interface Props {
  application: Application;
}

const repoName = (repo: string): string => {
  const parts = (repo && repo.replace(/https?:\/\//g, '').split('/')) || [];
  if (parts.length >= 2) {
    return `${parts[parts.length - 2]}/${parts[parts.length - 1]}`;
  } else {
    return repo;
  }
};

const ApplicationCard = ({ application }: Props) => (
  <Card bordered bodyStyle={{ padding: 8 }}>
    <Row style={{ textAlign: 'center', height: 120 }} type="flex" align="middle">
      <Col span={24} style={{ paddingBottom: 8 }}>
        {application.logo && <img alt="logo" src={application.logo} style={{ height: 64, objectFit: 'contain' }} />}
        {!!!application.logo && <Icon style={{ fontSize: 35 }} type="code" />}
      </Col>
      <Col span={8}>
        <b style={{ fontSize: 12 }}>{application.language || 'Java'}</b>
        <br />
        LANGUAGE
      </Col>
      <Col span={16}>
        <b style={{ fontSize: 12 }}>
          {application.repository && (
            <a style={{ color: '#1DA57A' }} href={application.repository}>
              {repoName(application.repository!)}
            </a>
          )}
          {!!!application.repository && <span style={{ color: '#000000' }}>N/A</span>}
        </b>
        <br />
        REPOSITORY
      </Col>
    </Row>
  </Card>
);

export default ApplicationCard;
