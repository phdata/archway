import { Col, Row, Icon } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import moment from 'moment';
import { Colors, ListingSearchBar, WorkspaceList } from '../../components';
import { WorkspaceSearchResult } from '../../models/Workspace';
import { Profile } from '../../models/Profile';
import * as actions from './actions';
import * as selectors from './selectors';

/* tslint:disable:no-var-requires */
const { CSVLink } = require('react-csv');
/* tslint:disable:no-var-requires */
const router = require('connected-react-router/immutable');

interface Props {
  fetching: boolean;
  listingMode: string;
  workspaceList: WorkspaceSearchResult[];
  filters: { filter: string; behaviors: string[]; statuses: string[] };
  profile: Profile;
  updateFilter: (filter: string, behavior: string[], statuses: string[]) => void;
  openWorkspace: (id: number) => void;
  listWorkspaces: () => void;
  setListingMode: (mode: string) => void;
}

class WorkspaceListing extends React.PureComponent<Props> {
  public componentDidMount() {
    this.props.listWorkspaces();
  }

  public generateCSV(workspaceList: WorkspaceSearchResult[]) {
    return workspaceList.map(workspace => ({
      Name: workspace.name,
      Status: workspace.status,
      'Date Requested': moment(workspace.date_requested).format('YYYY-MM-DD'),
      'Date Fully Approved': moment(workspace.date_fully_approved).format('YYYY-MM-DD'),
      'Disk Allocated': workspace.total_disk_allocated_in_gb,
      'Max Cores': workspace.total_max_cores,
      'Max Memory': workspace.total_max_memory_in_gb,
    }));
  }

  public render() {
    const {
      fetching,
      listingMode,
      workspaceList,
      filters,
      openWorkspace,
      setListingMode,
      profile,
      updateFilter,
    } = this.props;
    const { filter, behaviors, statuses } = filters;
    const emptyText =
      !filter && behaviors.length === 2 && statuses.length === 3
        ? 'No workspaces yet. Create one from the link on the left.'
        : 'No workspaces found';

    return (
      <div>
        <h1 style={{ textAlign: 'center', margin: 0, padding: 24 }}>Workspaces</h1>
        <Row type="flex">
          <Col span={24} xxl={{ span: 12, offset: 6 }}>
            <ListingSearchBar filters={filters} updateFilter={updateFilter} />
          </Col>
        </Row>
        {workspaceList && workspaceList.length > 0 && profile.permissions && profile.permissions.platform_operations && (
          <Row type="flex" style={{ marginTop: 12, fontSize: 12 }} justify="center">
            <Col>
              <CSVLink data={this.generateCSV(workspaceList)} filename={`workspace.csv`}>
                <Icon style={{ fontSize: 16 }} type="file-excel" theme="twoTone" twoToneColor={Colors.Green.string()} />{' '}
                DOWNLOAD AS REPORT
              </CSVLink>
            </Col>
          </Row>
        )}
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
    listingMode: selectors.SearchBar.getListingMode(),
    workspaceList: selectors.SearchBar.workspaceList(),
    filters: selectors.SearchBar.getListFilters(),
    profile: selectors.getProfile(),
  });

const mapDispatchToProps = (dispatch: Dispatch<any>) => ({
  openWorkspace: (id: number) => dispatch(router.push(`/workspaces/${id}`)),
  updateFilter: (filter: string, behavior: string[], statuses: string[]) =>
    dispatch(actions.filterWorkspaces(filter, behavior, statuses)),
  listWorkspaces: () => dispatch(actions.listAllWorkspaces()),
  setListingMode: (mode: string) => dispatch(actions.setListingMode(mode)),
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(WorkspaceListing);
