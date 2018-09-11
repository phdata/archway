import * as React from 'react';
import { Card, List, Avatar } from 'antd';

import Color from './Colors';
import { Workspace } from '../WorkspaceListing/Workspace';

const WorkspaceStatus = () => (
  <div
    style={{
      width: '50%',
      backgroundColor: 'green',
      display: 'inline-block',
      color: 'white',
      marginTop: 5
    }}>
    Approved
  </div>
);

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
  const gridStyle = { width: '33%', textAlign: 'center' };
  const {
    id,
    name,
  } = workspace;
  const onClick = () => onSelected(id);
  return (
    // @ts-ignore
    <List.Item>
      <Card
        style={{ textAlign: 'center' }}
        onClick={onClick}
        hoverable={true}>
        <Avatar icon="user" />
        <h2 style={{ textAlign: 'center' }}>{name}</h2>
        <h4>partially approved</h4>
        <WorkspaceDetail label="DBs" value={1} />
        <WorkspaceDetail label="Pools" value={1} />
        <WorkspaceDetail label="Topics" value={0} />
        <WorkspaceDetail label="Apps" value={1} />
      </Card>
    </List.Item>
  );
};

export default WorkspaceListItem;