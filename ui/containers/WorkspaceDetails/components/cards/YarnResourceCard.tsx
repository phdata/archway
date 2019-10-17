import * as React from 'react';
import { Card, Row, Col, Button, Spin } from 'antd';

import { ResourcePool } from '../../../../models/Workspace';
import { ModalType } from '../../../../constants';

interface Props {
  resource: ResourcePool;
  resourcePoolLoading: boolean;
  showModal: (e: React.MouseEvent, type: ModalType) => void;
}

const YarnResourceCard = ({ resource, resourcePoolLoading, showModal }: Props) => (
  <Card bordered bodyStyle={{ padding: 8 }}>
    <Row style={{ textAlign: 'center', height: 150 }} type="flex" align="middle">
      <Col span={24}>YARN RESOURCES</Col>
      {resourcePoolLoading ? (
        <div style={{ width: '100%', textAlign: 'center' }}>
          <Spin tip="Updating" />
        </div>
      ) : (
        <React.Fragment>
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
          <Col span={24}>
            <Button type="primary" onClick={e => showModal(e, ModalType.ModifyCoreMemory)}>
              Update
            </Button>
          </Col>
        </React.Fragment>
      )}
    </Row>
  </Card>
);

export default YarnResourceCard;
