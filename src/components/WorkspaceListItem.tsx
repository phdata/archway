import { Card, Col, Icon, List, Row } from 'antd';
import * as React from 'react';
import { Workspace } from '../types/Workspace';

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
    status = '',
  } = workspace;

  const onClick = () => onSelected(id);

  const ApprovalMessage = () => {
    let color = '';
    switch (status.toLowerCase()) {
      case 'pending':
        color = '#CFB2B0';
        break;
      case 'rejected':
        color = '#7B2D26';
        break;
      case 'approved':
        color = '#0B7A75';
        break;
      default:
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
        {status}
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
