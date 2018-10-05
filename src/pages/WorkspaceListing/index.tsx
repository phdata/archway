import { Card, Col, Input, List, Row, Checkbox } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import Behavior from '../../components/Behavior';
import WorkspaceListItem from '../../components/WorkspaceListItem';
import { Workspace } from '../../types/Workspace';
import * as actions from './actions';
import * as selectors from './selectors';
import FieldLabel from '../../components/FieldLabel';

/* tslint:disable:no-var-requires */
const router = require('connected-react-router/immutable');

interface Props {
  fetching: boolean;
  workspaceList: Workspace[];
  filters: { filter: string, behaviors: string[] };
  updateFilter: (filter: string, behavior: string[]) => void;
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
    this.props.updateFilter(event.target.value, this.props.filters.behaviors);
  }

  public behaviorChanged(behavior: string, checked: boolean) {
    const behaviors = this.props.filters.behaviors;
    if (checked) {
      behaviors.push(behavior);
    } else {
      behaviors.splice(behaviors.indexOf(behavior), 1);
    }

    this.props.updateFilter(this.props.filters.filter, this.props.filters.behaviors);
  }

  public componentDidMount() {
    this.props.listWorkspaces();
  }

  public render() {
    const { fetching, workspaceList, filters: { filter, behaviors }, openWorkspace } = this.props;
    const renderItem = (workspace: Workspace) => <WorkspaceListItem workspace={workspace} onSelected={openWorkspace} />;
    return (
      <div>
        <h1 style={{ textAlign: 'center' }}>Workspaces</h1>
        <Row>
          <Col span={24} lg={{ span: 12, offset: 6 }}>
            <Card>
              <Row gutter={12} type="flex">
                <Col
                  span={12}
                  style={{
                      display: 'flex',
                      flex: 1,
                      flexDirection: 'column',
                      justifyContent: 'space-between'
                    }}>
                  <div>
                    <FieldLabel>FILTER</FieldLabel>
                    <Input.Search
                      value={filter}
                      onChange={this.filterUpdated}
                      placeholder="find a workspace..."
                      size="large"
                    />
                  </div>
                  <div>
                    <FieldLabel>STATUS</FieldLabel>
                    <div style={{ display: 'flex', justifyContent: 'space-around' }}>
                      <Checkbox style={{ fontSize: 12 }} name="phi_data">APPROVED</Checkbox>
                      <Checkbox style={{ fontSize: 12 }} name="pci_data">PENDING</Checkbox>
                      <Checkbox style={{ fontSize: 12 }} name="pii_data">REJECTED</Checkbox>
                    </div>
                  </div>
                </Col>
                <Col span={12}>
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
        <Row style={{ marginTop: 12 }}>
          <Col span={24}>
            <List
              grid={{ gutter: 16, lg: 2, column: 1 }}
              locale={{ emptyText: 'No workspaces yet. Create one from the link on the left.' }}
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
  });

const mapDispatchToProps = (dispatch: Dispatch<any>) => ({
  openWorkspace: (id: number) => dispatch(router.push(`/workspaces/${id}`)),
  updateFilter: (filter: string, behavior: string[]) => dispatch(actions.filterWorkspaces(filter, behavior)),
  listWorkspaces: () => dispatch(actions.listAllWorkspaces()),
});

export default connect(mapStateToProps, mapDispatchToProps)(WorkspaceList);
