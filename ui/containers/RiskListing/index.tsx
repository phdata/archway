import * as React from 'react';
import { Card, Col, List, Row, Table, Input, Checkbox } from 'antd';
import { connect } from 'react-redux';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import { Behavior, WorkspaceListItem, ListCardToggle, workspaceColumns, FieldLabel } from '../../components';
import { WorkspaceSearchResult } from '../../models/Workspace';
import { FeatureService } from '../../service/FeatureService';
import { FeatureFlagType } from '../../constants';
import { Filters } from '../../models/Listing';
import { workspaceStatuses, workspaceBehaviors, behaviorProperties } from '../../constants';
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
  constructor(props: Props) {
    super(props);

    this.filterUpdated = this.filterUpdated.bind(this);
    this.behaviorChanged = this.behaviorChanged.bind(this);
    this.renderItem = this.renderItem.bind(this);
    this.renderBehaviors = this.renderBehaviors.bind(this);
  }

  public componentDidMount() {
    const {
      listWorkspaces,
      updateFilter,
      filters: { filter, statuses },
    } = this.props;
    const featureService = new FeatureService();

    if (!featureService.isEnabled(FeatureFlagType.CustomTemplates) && workspaceBehaviors.includes('custom')) {
      workspaceBehaviors.splice(workspaceBehaviors.indexOf('custom'), 1);
      updateFilter(filter, workspaceBehaviors, statuses);
    }
    listWorkspaces();
  }

  public filterUpdated(event: React.ChangeEvent<HTMLInputElement>) {
    const {
      filters: { behaviors, statuses },
      updateFilter,
    } = this.props;

    updateFilter(event.target.value, behaviors, statuses);
  }

  public behaviorChanged(behavior: string, checked: boolean) {
    const {
      filters: { filter, behaviors, statuses },
      updateFilter,
    } = this.props;

    if (checked) {
      behaviors.push(behavior);
    } else {
      behaviors.splice(behaviors.indexOf(behavior), 1);
    }

    updateFilter(filter, behaviors, statuses);
  }

  public statusChanged(status: string, checked: boolean) {
    const {
      filters: { filter, behaviors, statuses },
    } = this.props;

    if (checked) {
      statuses.push(status);
    } else {
      statuses.splice(statuses.indexOf(status), 1);
    }

    this.props.updateFilter(filter, behaviors, statuses);
  }

  public renderItem(workspace: WorkspaceSearchResult) {
    const { openWorkspace } = this.props;
    return <WorkspaceListItem workspace={workspace} onSelected={openWorkspace} />;
  }

  public renderBehaviors(behaviors: string[]) {
    return workspaceBehaviors.map((behavior, index) => (
      <Col span={8} key={index}>
        <Behavior
          behaviorKey={behavior}
          selected={behaviors.includes(behavior)}
          onChange={this.behaviorChanged}
          icon={behaviorProperties[behavior].icon}
          title={behaviorProperties[behavior].title}
        />
      </Col>
    ));
  }

  public render() {
    const {
      fetching,
      workspaceList,
      openWorkspace,
      listingMode,
      setListingMode,
      filters: { filter, behaviors, statuses },
    } = this.props;
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
          <Col span={24} xxl={{ span: 16, offset: 4 }} style={{ display: 'flex', justifyContent: 'center' }}>
            <Card style={{ display: 'flex', alignItems: 'center' }}>
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                {riskInfo.map(({ type, value }) => (
                  <div style={{ padding: '0 20px' }} key={type}>
                    <span style={{ fontSize: 20 }}>{value}</span>&nbsp;
                    {type}
                  </div>
                ))}
              </div>
            </Card>
            <Card style={{ width: '100%' }}>
              <Row gutter={12} type="flex">
                <Col
                  span={24}
                  lg={12}
                  style={{
                    display: 'flex',
                    flex: 1,
                    flexDirection: 'column',
                    justifyContent: 'space-around',
                  }}
                >
                  <div>
                    <FieldLabel>FILTER</FieldLabel>
                    <Input.Search value={filter} onChange={this.filterUpdated} placeholder="find a workspace..." />
                  </div>
                  <div>
                    <FieldLabel>STATUS</FieldLabel>
                    <div style={{ display: 'flex', justifyContent: 'space-around' }}>
                      {workspaceStatuses.map((status: string) => (
                        <Checkbox
                          key={status}
                          style={{ fontSize: 12, textTransform: 'uppercase' }}
                          defaultChecked
                          name={status}
                          checked={statuses.includes(status)}
                          onChange={(e: any) => this.statusChanged(status, e.target.checked)}
                        >
                          {status}
                        </Checkbox>
                      ))}
                    </div>
                  </div>
                </Col>
                <Col span={24} lg={12}>
                  <FieldLabel>BEHAVIOR</FieldLabel>
                  <Row type="flex" style={{ flexDirection: 'row', justifyContent: 'center' }} gutter={12}>
                    {this.renderBehaviors(behaviors)}
                  </Row>
                </Col>
              </Row>
            </Card>
          </Col>
        </Row>

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
                renderItem={this.renderItem}
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
    fetching: selectors.isFetchingRiskWorkspaces(),
    workspaceList: selectors.riskWorkspaceList(),
    listingMode: selectors.getListingMode(),
    filters: selectors.getListFilters(),
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
