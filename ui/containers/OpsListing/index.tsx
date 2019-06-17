import { Col, List, Row, Table } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import { WorkspaceListItem, ListCardToggle, workspaceColumns } from '../../components';
import { WorkspaceSearchResult } from '../../models/Workspace';
import * as actions from './actions';
import * as selectors from './selectors';

/* tslint:disable:no-var-requires */
const router = require('connected-react-router/immutable');

interface Props {
  fetching: boolean;
  listingMode: string;
  workspaceList: WorkspaceSearchResult[];
  openWorkspace: (id: number) => void;
  listWorkspaces: () => void;
  setListingMode: (mode: string) => void;
}

class OpsListing extends React.PureComponent<Props> {
  public componentDidMount() {
    this.props.listWorkspaces();
  }

  public render() {
    const { fetching, workspaceList, openWorkspace, listingMode, setListingMode } = this.props;
    const emptyText = 'No workspaces found';
    const renderItem = (workspace: WorkspaceSearchResult) => (
      <WorkspaceListItem workspace={workspace} onSelected={openWorkspace} />
    );

    return (
      <div>
        <h1 style={{ textAlign: 'center' }}>Operations</h1>
        <ListCardToggle
          style={{ margin: '12px 0' }}
          selectedMode={listingMode}
          onSelect={mode => setListingMode(mode)}
        />
        {listingMode === 'cards' && (
          <Row style={{ marginTop: 12 }}>
            <Col span={24}>
              <List
                grid={{ gutter: 12, column: 2 }}
                locale={{ emptyText }}
                loading={fetching}
                dataSource={workspaceList}
                renderItem={renderItem}
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
      </div>
    );
  }
}

const mapStateToProps = () =>
  createStructuredSelector({
    fetching: selectors.isFetchingOpsWorkspaces(),
    workspaceList: selectors.opsWorkspaceList(),
    listingMode: selectors.getListingMode(),
  });

const mapDispatchToProps = (dispatch: Dispatch<any>) => ({
  openWorkspace: (id: number) => dispatch(router.push(`/workspaces/${id}`)),
  listWorkspaces: () => dispatch(actions.listOpsWorkspaces()),
  setListingMode: (mode: string) => dispatch(actions.setListingMode(mode)),
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(OpsListing);
