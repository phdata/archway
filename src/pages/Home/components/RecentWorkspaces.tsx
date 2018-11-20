import * as React from 'react';
import { List } from 'antd';

import WorkspaceListItem from '../../../components/WorkspaceListItem';
import { WorkspaceSearchResult } from '../../../types/Workspace';

interface Props {
  onSelectWorkspace: (id: number) => void;
}

const RecentWorkspaces = ({ onSelectWorkspace }: Props) => {
  const recentWorkspacesKey = 'recentWorkspaces';
  let recentWorkspaces = [];
  try {
    const recentWorkspacesJson = localStorage.getItem(recentWorkspacesKey) || '[]';
    recentWorkspaces = JSON.parse(recentWorkspacesJson);
  } catch (e) {
    //
  }
  const renderItem = (workspace: WorkspaceSearchResult) => (
    <WorkspaceListItem
      workspace={workspace}
      onSelected={() => onSelectWorkspace(workspace.id)}
    />
  );

  return (
    <div>
      <h3 style={{ paddingTop: '16px' }}>
        {recentWorkspaces.length > 0 ? 'RECENT WORKSPACES' : 'NO RECENT WORKSPACES'}
      </h3>
      <List
        grid={{ gutter: 16, column: 1, lg: 2 }}
        dataSource={recentWorkspaces}
        renderItem={renderItem}
      />
    </div>
  );
};

export default RecentWorkspaces;
