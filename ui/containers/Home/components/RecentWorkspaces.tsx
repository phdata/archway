import * as React from 'react';
import { List } from 'antd';

import WorkspaceListItem from '../../../components/WorkspaceListItem';
import { Workspace } from '../../../models/Workspace';

interface Props {
  workspaces: Workspace[];
  onSelectWorkspace: (id: number) => void;
}

const RecentWorkspaces = ({ workspaces, onSelectWorkspace }: Props) => {
  const renderItem = (workspace: Workspace) => (
    <WorkspaceListItem workspace={workspace} onSelected={() => onSelectWorkspace(workspace.id)} />
  );

  return (
    <div style={{ padding: '0 12px' }}>
      <h3 style={{ paddingTop: '16px' }}>{workspaces.length > 0 ? 'RECENT WORKSPACES' : 'NO RECENT WORKSPACES'}</h3>
      <List grid={{ gutter: 25, column: 1, lg: 2 }} dataSource={workspaces} renderItem={renderItem} />
    </div>
  );
};

export default RecentWorkspaces;
