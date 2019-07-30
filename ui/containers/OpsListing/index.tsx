import { Col, Row } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import { WorkspaceList, ListingSearchBar } from '../../components';
import { WorkspaceSearchResult } from '../../models/Workspace';
import { Filters } from '../../models/Listing';
import * as actions from './actions';
import * as selectors from './selectors';
/* tslint:disable:no-var-requires */
const router = require('connected-react-router/immutable');

export interface Props {
  fetching: boolean;
  listingMode: string;
  workspaceList: WorkspaceSearchResult[];
  filters: Filters;

  openWorkspace: (id: number) => void;
  listWorkspaces: () => void;
  setListingMode: (mode: string) => void;
  updateFilter: (filter: string, behavior: string[], statuses: string[]) => void;
}

class OpsListing extends React.PureComponent<Props> {
  public componentDidMount() {
    this.props.listWorkspaces();
  }

  public render() {
    const { fetching, workspaceList, openWorkspace, listingMode, setListingMode, filters, updateFilter } = this.props;
    const emptyText = 'No workspaces found';

    return (
      <div>
        <h1 style={{ textAlign: 'center', margin: 0, padding: 24 }}>Operations</h1>
        <Row type="flex">
          <Col span={24} xxl={{ span: 12, offset: 6 }}>
            <ListingSearchBar filters={filters} updateFilter={updateFilter} />
          </Col>
        </Row>

        <WorkspaceList
          workspaceList={workspaceList}
          listingMode={listingMode}
          emptyText={emptyText}
          fetching={fetching}
          setListingMode={setListingMode}
          openWorkspace={openWorkspace}
        />
      </div>
    );
  }
}

const mapStateToProps = () =>
  createStructuredSelector({
    fetching: selectors.SearchBar.isFetchingWorkspaces(),
    workspaceList: selectors.SearchBar.workspaceList(),
    listingMode: selectors.SearchBar.getListingMode(),
    filters: selectors.SearchBar.getListFilters(),
  });

const mapDispatchToProps = (dispatch: Dispatch<any>) => ({
  openWorkspace: (id: number) => dispatch(router.push(`/workspaces/${id}`)),
  listWorkspaces: () => dispatch(actions.listOpsWorkspaces()),
  setListingMode: (mode: string) => dispatch(actions.setListingMode(mode)),
  updateFilter: (filter: string, behaviors: string[], statuses: string[]) =>
    dispatch(actions.filterWorkspaces(filter, behaviors, statuses)),
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(OpsListing);
