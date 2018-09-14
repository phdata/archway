import * as React from 'react';
import { Card, List, Icon, Tag } from 'antd';

import Color from './Colors';
import { Workspace } from '../WorkspaceListing/Workspace';

interface DetailProps {
  label: string
  value: any
}

const WorkspaceDetail = ({ label, value }: DetailProps) => (
  <Card.Grid style={{ width: '25%', textAlign: 'center', boxShadow: 'none' }}>
    <h2 style={{ color: Color.Orange.rgb().string(), fontWeight: 100, textAlign: 'center' }}>
      {value}
    </h2>
    <h5 style={{ textAlign: 'center' }}>
      {label}
    </h5>
  </Card.Grid>
)

interface Props {
  workspace: Workspace
  onSelected: (id: number) => void
}

const WorkspaceListItem = ({ workspace, onSelected }: Props) => {
  const {
    id,
    name,
    behavior,
    summary,
    approvals
  } = workspace;

  const onClick = () => onSelected(id);

  const approvalMessage = () => {
    let message = '';
    let color = '';
    
    if(!approvals) {
      message = 'all approvals needed';
      color = 'red';
    } else if(!approvals.infra || !approvals.risk) {
      message = 'partially approved';
      color = 'orange';
    }

    return message === '' ? false : <Tag color={color}>{message}</Tag>;
  }

  return (
    <List.Item>
      <Card
        bordered={true}
        style={{ textAlign: 'center' }}
        onClick={onClick}
        hoverable={true}>
        <h2 style={{ textAlign: 'center' }}>{name}</h2>
        <div style={{ marginTop: 10, marginBottom: 10 }}>
          <Icon style={{ fontSize: 42 }} type={behavior === 'simple' ? 'team' : 'deployment-unit' } />
          <div style={{ fontSize: 12, textTransform: 'uppercase' }}>{behavior} workspace</div>
        </div>
        <h3>{summary}</h3>
        {approvalMessage()}
      </Card>
    </List.Item>
  );
};

export default WorkspaceListItem;