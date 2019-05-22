import * as React from 'react';
import { Card, Row, Col } from 'antd';

import { ResourcePool } from '../../../../models/Workspace';

interface Props {
  resource: ResourcePool;
}

const YarnResourceCard = ({ resource }: Props) => (
  <Card bordered bodyStyle={{ padding: 8 }}>
    <Row style={{ textAlign: 'center', height: 120 }} type="flex" align="middle">
      <Col span={24}>
        YARN RESOURCES
      </Col>
      <Col span={24}>
        <b style={{ fontSize: 12 }}>{resource.pool_name}</b>
        <br />
        QUEUE NAME
      </Col>
      <Col span={12}>
        <b>{resource.max_cores}</b>
        <br />
        MAX CORES
      </Col>
      <Col span={12}>
        <b>{resource.max_memory_in_gb}GB</b>
        <br />
        MAX MEMORY
      </Col>
    </Row>
  </Card>
);

export default YarnResourceCard;
