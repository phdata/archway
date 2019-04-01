import * as React from 'react';
import { List } from 'antd';

import WorkspaceListItem from '../../../components/WorkspaceListItem';
import { WorkspaceSearchResult } from '../../../models/Workspace';

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
      <div style={{ padding: '0 25px' }}>
        <List
          grid={{ gutter: 25, column: 1, lg: 2 }}
          dataSource={recentWorkspaces}
          renderItem={renderItem}
        />
      </div>
    </div>
  );
};

export default RecentWorkspaces;
