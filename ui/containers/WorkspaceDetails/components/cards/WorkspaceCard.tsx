import * as React from 'react';
import { Card, Button } from 'antd';

interface Props {
  children?: any;
  onDeleteWorkspace: (e: React.MouseEvent) => void;
}

const WorkspaceCard = ({ children, onDeleteWorkspace }: Props) => {
  return (
    <Card style={{ height: '100%' }} bordered>
      <div style={{ textAlign: 'center', fontSize: 17 }}>{children}</div>
      <div
        style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          fontWeight: 300,
          padding: '32px 0 48px 0',
        }}
      >
        <Button type="primary" onClick={onDeleteWorkspace}>
          Delete Workspace
        </Button>
      </div>
    </Card>
  );
};

export default WorkspaceCard;
