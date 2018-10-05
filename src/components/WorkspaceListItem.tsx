import { Card, Col, Icon, List, Row } from 'antd';
import * as React from 'react';
import { Workspace } from '../types/Workspace';
import Colors from './Colors';

interface Props {
    workspace: Workspace;
    onSelected: (id: number) => void;
}

const WorkspaceListItem = ({ workspace, onSelected }: Props) => {
  const {
    id,
    name,
    behavior,
    summary,
    approvals,
  } = workspace;

  const onClick = () => onSelected(id);

  const ApprovalMessage = () => {
    let message = 'approved';
    let color = Colors.Green.string();

    if (!approvals) {
      message = 'pending';
      color = 'red';
    } else if (!approvals.infra || !approvals.risk) {
      message = 'pending';
      color = 'orange';
    }

    return (
      <div
        style={{
            textTransform: 'uppercase',
            position: 'absolute',
            top: 20,
            left: 20,
            fontSize: 14,
            fontWeight: 100,
            color,
          }}>
        {message}
      </div>
    );
  };

  return (
    <List.Item style={{ paddingLeft: 12, paddingRight: 12 }}>
      <Card
        bordered={true}
        onClick={onClick}
        hoverable={true}
        bodyStyle={{ paddingTop: 50, paddingBottom: 50, textAlign: 'center' }}>
        <ApprovalMessage />
        <Row type="flex">
          <Col span={24}>
            <div style={{ fontSize: 26 }}>{name}</div>
            <div style={{ marginTop: 10, marginBottom: 10, textAlign: 'center' }}>
              <Icon style={{ fontSize: 20 }} type={behavior === 'simple' ? 'team' : 'deployment-unit'} />
              <div style={{ fontSize: 12, textTransform: 'uppercase' }}>{behavior} workspace</div>
            </div>
            <p>{summary}</p>
          </Col>
        </Row>
      </Card>
    </List.Item>
  );
};

export default WorkspaceListItem;
