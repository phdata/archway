import { Card, Col, Input, List, Row, Checkbox, Icon } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import * as moment from 'moment';
import { Behavior, WorkspaceListItem } from '../../components';
import { Workspace } from '../../types/Workspace';
import * as actions from './actions';
import * as selectors from './selectors';
import FieldLabel from '../../components/FieldLabel';
import { Profile } from '../../types/Profile';
import { Cluster } from '../../types/Cluster';
import Colors from '../../components/Colors';

/* tslint:disable:no-var-requires */
const { CSVLink } = require('react-csv');
/* tslint:disable:no-var-requires */
const router = require('connected-react-router/immutable');

interface Props {
  fetching: boolean;
  workspaceList: Workspace[];
  filters: { filter: string, behaviors: string[], statuses: string[] };
  profile: Profile;
  cluster: Cluster;
  updateFilter: (filter: string, behavior: string[], statuses: string[]) => void;
  openWorkspace: (id: number) => void;
  listWorkspaces: () => void;
}

class WorkspaceList extends React.PureComponent<Props> {
  constructor(props: Props) {
    super(props);

    this.filterUpdated = this.filterUpdated.bind(this);
    this.behaviorChanged = this.behaviorChanged.bind(this);
  }

  public filterUpdated(event: React.ChangeEvent<HTMLInputElement>) {
    this.props.updateFilter(
      event.target.value,
      this.props.filters.behaviors,
      this.props.filters.statuses,
    );
  }

  public behaviorChanged(behavior: string, checked: boolean) {
    const behaviors = this.props.filters.behaviors;
    if (checked) {
      behaviors.push(behavior);
    } else {
      behaviors.splice(behaviors.indexOf(behavior), 1);
    }

    this.props.updateFilter(
      this.props.filters.filter,
      this.props.filters.behaviors,
      this.props.filters.statuses,
    );
  }

  public statusChanged(status: string, checked: boolean) {
    const statuses = this.props.filters.statuses;
    if (checked) {
      statuses.push(status);
    } else {
      statuses.splice(statuses.indexOf(status), 1);
    }

    this.props.updateFilter(
      this.props.filters.filter,
      this.props.filters.behaviors,
      this.props.filters.statuses,
    );
  }

  public componentDidMount() {
    this.props.listWorkspaces();
  }

  public generateCSV(workspaceList: Workspace[]) {
    return workspaceList.map((workspace) => {
      let fullApprovalDate = '';
      let diskAllocated = '';
      let maxCores = '';
      let maxMemory = '';

      const { approvals, data, processing } = workspace;
      if (approvals && approvals.infra && approvals.infra.approval_time) {
        fullApprovalDate = moment(approvals.infra.approval_time).format('YYYY-MM-DD');
      }
      if (approvals && approvals.risk && approvals.risk.approval_time) {
        const approvalDate = moment(approvals.risk.approval_time).format('YYYY-MM-DD');
        if (!fullApprovalDate || fullApprovalDate < approvalDate) {
          fullApprovalDate = approvalDate;
        }
      }
      if (data && data.length > 0) {
        diskAllocated = `${data[0].size_in_gb}`;
      }
      if (processing && processing.length > 0) {
        maxCores = `${processing[0].max_cores}`;
        maxMemory = `${processing[0].max_memory_in_gb}`;
      }

      return {
        'Name': workspace.name,
        'Status': workspace.status,
        'Date Requested': moment(workspace.requested_date).format('YYYY-MM-DD'),
        'Date Fully Approved': fullApprovalDate,
        'Disk Allocated': diskAllocated,
        'Max Cores': maxCores,
        'Max Memory': maxMemory,
      };
    });
  }

  public render() {
    const {
      fetching,
      workspaceList,
      filters: { filter, behaviors, statuses },
      openWorkspace,
      profile,
      cluster,
    } = this.props;
    const allStatuses: string[] = ['Approved', 'Pending', 'Rejected'];
    const emptyText = (!filter && behaviors.length === 2 && statuses.length === 3)
      ? 'No workspaces yet. Create one from the link on the left.'
      : 'No workspaces found';
    const renderItem = (workspace: Workspace) => <WorkspaceListItem workspace={workspace} onSelected={openWorkspace} />;

    return (
      <div>
        <h1 style={{ textAlign: 'center' }}>Workspaces</h1>
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
                    }}>
                  <div>
                    <FieldLabel>FILTER</FieldLabel>
                    <Input.Search
                      value={filter}
                      onChange={this.filterUpdated}
                      placeholder="find a workspace..."
                    />
                  </div>
                  <div>
                    <FieldLabel>STATUS</FieldLabel>
                    <div style={{ display: 'flex', justifyContent: 'space-around' }}>
                      {allStatuses.map((status: string) => (
                        <Checkbox
                          key={status}
                          style={{ fontSize: 12 }}
                          defaultChecked
                          name={status.toLowerCase()}
                          checked={statuses.includes(status)}
                          onChange={(e: any) => this.statusChanged(status, e.target.checked)}
                        >
                          {status.toUpperCase()}
                        </Checkbox>
                      ))}
                    </div>
                  </div>
                </Col>
                <Col span={24} lg={12}>
                  <FieldLabel>BEHAVIOR</FieldLabel>
                  <Row type="flex" style={{ flexDirection: 'row' }} gutter={12}>
                    <Col span={12}>
                      <Behavior
                        behaviorKey="structured"
                        selected={behaviors.indexOf('structured') >= 0}
                        onChange={this.behaviorChanged}
                        icon="deployment-unit"
                        title="Structured" />
                        </Col>
                    <Col span={12}>
                      <Behavior
                        behaviorKey="simple"
                        selected={behaviors.indexOf('simple') >= 0}
                        onChange={this.behaviorChanged}
                        icon="team"
                        title="Simple" />
                    </Col>
                  </Row>
                </Col>
              </Row>
            </Card>
          </Col>
        </Row>
        {workspaceList && workspaceList.length > 0 &&
          profile.permissions && profile.permissions.platform_operations && (
          <Row type="flex" style={{ marginTop: 12, fontSize: 12 }} justify="center">
            <Col>
              <CSVLink data={this.generateCSV(workspaceList)} filename={`${cluster.name} - Workspaces.csv`}>
                <Icon
                  style={{ fontSize: 16 }}
                  type="file-excel"
                  theme="twoTone"
                  twoToneColor={Colors.Green.string()} />
                {' '}DOWNLOAD AS REPORT
              </CSVLink>
            </Col>
          </Row>
        )}
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
      </div>
    );
  }
}

const mapStateToProps = () =>
  createStructuredSelector({
    fetching: selectors.isFetchingWorkspaces(),
    workspaceList: selectors.workspaceList(),
    filters: selectors.getListFilters(),
    profile: selectors.getProfile(),
    cluster: selectors.getCluster(),
  });

const mapDispatchToProps = (dispatch: Dispatch<any>) => ({
  openWorkspace: (id: number) => dispatch(router.push(`/workspaces/${id}`)),
  updateFilter: (
    filter: string,
    behavior: string[],
    statuses: string[],
  ) => dispatch(actions.filterWorkspaces(filter, behavior, statuses)),
  listWorkspaces: () => dispatch(actions.listAllWorkspaces()),
});

export default connect(mapStateToProps, mapDispatchToProps)(WorkspaceList);
