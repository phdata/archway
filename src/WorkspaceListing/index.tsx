import { Card, Checkbox, Col, Input, List, Row } from 'antd';
import { CheckboxValueType } from 'antd/lib/checkbox/Group';
import * as React from 'react';
import { connect } from 'react-redux';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import WorkspaceListItem from '../Common/WorkspaceListItem';
import { filterWorkspaces } from './actions';
import { isFetchingWorkspaces, workspaceList } from './selectors';
import { Workspace } from './Workspace';
import Behavior from '../Common/Behavior';
const router = require('connected-react-router/immutable');

interface Props {
  fetching: boolean
  workspaceList: Array<Workspace>
  updateFilter: (filter: string, behavior: string[]) => void
  openWorkspace: (id: number) => void
}

interface State {
  filter: string
  behavior: string[]
}

class WorkspaceList extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      filter: '',
      behavior: []
    };

    this.filterUpdated = this.filterUpdated.bind(this);
    this.behaviorChanged = this.behaviorChanged.bind(this);
  }

  filterUpdated(event: React.ChangeEvent<HTMLInputElement>) {
    const filter = event.target.value;
    this.setState({
      ...this.state,
      filter
    });
    this.props.updateFilter(filter, this.state.behavior);
  }

  behaviorChanged(behavior: string, checked: boolean) {
    // this.setState({
    //   ...this.state,
    //   behavior
    // });
    // this.props.updateFilter(this.state.filter, behavior);
  }

  componentDidMount() {
    this.props.updateFilter('', []);
  }

  render() {
    const { fetching, workspaceList, openWorkspace } = this.props;
    const renderItem = (workspace: Workspace) => <WorkspaceListItem workspace={workspace} onSelected={openWorkspace} />
    return (
      <Row>
        <Col span={12} lg={8}>
          <Card>
            <Input.Search
              onChange={this.filterUpdated}
              placeholder="find a workspace..."
              size="large"
            />
            <h3 style={{ margin: 25, textAlign: 'center' }}>Behavior</h3>
          <Behavior
            behaviorKey="structured"
            selected={true}
            onChange={this.behaviorChanged}
            icon="deployment-unit"
            title="Structured" />
          <Behavior
            style={{ marginTop: 25 }}
            behaviorKey="simple"
            selected={true}
            onChange={this.behaviorChanged}
            icon="team"
            title="Simple" />
          </Card>
        </Col>
        <Col span={12} lg={16} offset={2}>
          <List
            grid={{ gutter: 16, column: 4 }}
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
    fetching: isFetchingWorkspaces(),
    workspaceList: workspaceList(),
  });

const mapDispatchToProps = (dispatch: Dispatch<any>) => ({
  openWorkspace: (id: number) => dispatch(router.push(`/workspaces/${id}`)),
  updateFilter: (filter: string, behavior: string[]) => dispatch(filterWorkspaces(filter, behavior))
});

export default connect(mapStateToProps, mapDispatchToProps)(WorkspaceList);