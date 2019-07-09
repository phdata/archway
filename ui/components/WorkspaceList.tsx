import * as React from 'react';
import { List, Table, Row, Col } from 'antd';

import { workspaceColumns, WorkspaceListItem, ListCardToggle } from './';
import { WorkspaceSearchResult } from '../models/Workspace';

interface Props {
  workspaceList: WorkspaceSearchResult[];
  listingMode: string;
  emptyText: string;
  fetching: boolean;

  setListingMode: (mode: string) => void;
  openWorkspace: (id: number) => void;
}

const WorkspaceList = ({ workspaceList, listingMode, emptyText, fetching, setListingMode, openWorkspace }: Props) => {
  return (
    <React.Fragment>
      <ListCardToggle style={{ margin: '12px 0' }} selectedMode={listingMode} onSelect={mode => setListingMode(mode)} />
      {listingMode === 'cards' && (
        <Row style={{ marginTop: 12 }}>
          <Col span={24}>
            <List
              grid={{ gutter: 12, md: 1, lg: 2 }}
              locale={{ emptyText }}
              loading={fetching}
              dataSource={workspaceList}
              renderItem={(workspace: WorkspaceSearchResult) => (
                <WorkspaceListItem workspace={workspace} onSelected={openWorkspace} />
              )}
            />
          </Col>
        </Row>
      )}
      {listingMode === 'list' && (
        <Table
          columns={workspaceColumns}
          dataSource={workspaceList}
          onRow={record => ({
            onClick: () => openWorkspace(record.id),
          })}
        />
      )}
    </React.Fragment>
  );
};

export default WorkspaceList;
