import { Col, List, Row, Table, Checkbox, Card, Input } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import { WorkspaceListItem, ListCardToggle, workspaceColumns, Behavior, FieldLabel } from '../../components';
import { WorkspaceSearchResult } from '../../models/Workspace';
import { FeatureService } from '../../service/FeatureService';
import { FeatureFlagType, workspaceBehaviors, behaviorProperties, workspaceStatuses } from '../../constants';
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

  openWorkspace: (id: number) => void;
  listWorkspaces: () => void;
  setListingMode: (mode: string) => void;
  updateFilter: (filter: string, behavior: string[], statuses: string[]) => void;
}

class OpsListing extends React.PureComponent<Props> {
  constructor(props: Props) {
    super(props);

    this.filterUpdated = this.filterUpdated.bind(this);
    this.behaviorChanged = this.behaviorChanged.bind(this);
    this.renderItem = this.renderItem.bind(this);
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
    const emptyText = 'No workspaces found';

    return (
      <div>
        <h1 style={{ textAlign: 'center', padding: 24 }}>Operations</h1>
        <Row type="flex">
          <Col span={24} xxl={{ span: 12, offset: 6 }}>
            <Card>
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
    fetching: selectors.isFetchingOpsWorkspaces(),
    workspaceList: selectors.opsWorkspaceList(),
    listingMode: selectors.getListingMode(),
    filters: selectors.getListFilters(),
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
