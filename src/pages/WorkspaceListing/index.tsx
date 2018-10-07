import { Card, Col, Input, List, Row } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import { Behavior, WorkspaceListItem } from '../../components';
import { Workspace } from '../../types/Workspace';
import * as actions from './actions';
import * as selectors from './selectors';

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
      <Row>
        <Col span={12} lg={6}>
          <Card>
            <Input.Search
              value={filter}
              onChange={this.filterUpdated}
              placeholder="find a workspace..."
              size="large"
            />
            <h3 style={{ margin: 25, textAlign: 'center' }}>Behavior</h3>
            <Row type="flex" justify="center">
              <Col span={12}>
                <Behavior
                  behaviorKey="structured"
                  selected={behaviors.indexOf('structured') >= 0}
                  onChange={this.behaviorChanged}
                  icon="deployment-unit"
                  title="Structured" />
                <Behavior
                  style={{ marginTop: 25 }}
                  behaviorKey="simple"
                  selected={behaviors.indexOf('simple') >= 0}
                  onChange={this.behaviorChanged}
                  icon="team"
                  title="Simple" />
              </Col>
            </Row>
          </Card>
        </Col>
        <Col span={11} lg={17} offset={1}>
          <List
            grid={{ gutter: 16, lg: 4, column: 1 }}
            locale={{ emptyText: 'No workspaces yet. Create one from the link on the left.' }}
            loading={fetching}
            dataSource={workspaceList}
            renderItem={renderItem}
          />
        </Col>
      </Row>
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
