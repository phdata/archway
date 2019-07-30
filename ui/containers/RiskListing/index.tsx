import * as React from 'react';
import { Card, Col, Row } from 'antd';
import { connect } from 'react-redux';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import { ListingSearchBar, WorkspaceList } from '../../components';
import { WorkspaceSearchResult } from '../../models/Workspace';
import { Filters } from '../../models/Listing';
import * as actions from './actions';
import * as selectors from './selectors';

/* tslint:disable:no-var-requires */
const router = require('connected-react-router/immutable');

interface Props {
  fetching: boolean;
  listingMode: string;
  workspaceList: WorkspaceSearchResult[];
  filters: Filters;

  updateFilter: (filter: string, behavior: string[], statuses: string[]) => void;
  openWorkspace: (id: number) => void;
  listWorkspaces: () => void;
  setListingMode: (mode: string) => void;
}

class RiskListing extends React.PureComponent<Props> {
  public componentDidMount() {
    this.props.listWorkspaces();
  }

  public render() {
    const { fetching, workspaceList, openWorkspace, listingMode, setListingMode, filters, updateFilter } = this.props;
    const { filter, behaviors, statuses } = filters;
    const emptyText =
      !filter && behaviors.length === 2 && statuses.length === 3
        ? 'No workspaces yet. Create one from the link on the left.'
        : 'No workspaces found';

    const pending = workspaceList.length;
    const pci = workspaceList.filter(({ pci_data }) => pci_data).length;
    const phi = workspaceList.filter(({ phi_data }) => phi_data).length;
    const pii = workspaceList.filter(({ pii_data }) => pii_data).length;
    const riskInfo = [
      { type: 'PENDING', value: pending },
      { type: 'PCI', value: pci },
      { type: 'PHI', value: phi },
      { type: 'PII', value: pii },
    ];

    return (
      <div>
        <h1 style={{ textAlign: 'center', margin: 0, padding: 24 }}>Risk/Compliance</h1>
        <Row type="flex">
          <Col span={4} xxl={{ span: 4, offset: 4 }}>
            <Card style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                {riskInfo.map(({ type, value }) => (
                  <div style={{ padding: '0 20px' }} key={type}>
                    <span style={{ fontSize: 20 }}>{value}</span>&nbsp;
                    {type}
                  </div>
                ))}
              </div>
            </Card>
          </Col>
          <Col span={20} xxl={{ span: 12 }}>
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
    fetching: selectors.searchBar.isFetchingWorkspaces(),
    workspaceList: selectors.searchBar.workspaceList(),
    listingMode: selectors.searchBar.getListingMode(),
    filters: selectors.searchBar.getListFilters(),
  });

const mapDispatchToProps = (dispatch: Dispatch<any>) => ({
  openWorkspace: (id: number) => dispatch(router.push(`/workspaces/${id}`)),
  listWorkspaces: () => dispatch(actions.listRiskWorkspaces()),
  setListingMode: (mode: string) => dispatch(actions.setListingMode(mode)),
  updateFilter: (filter: string, behaviors: string[], statuses: string[]) =>
    dispatch(actions.filterWorkspaces(filter, behaviors, statuses)),
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(RiskListing);
