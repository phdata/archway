import * as React from 'react';
import { Button, Checkbox, Row, Col, List, Avatar, Input, Form, Card, Tag } from 'antd';
import { connect } from 'react-redux';
const router = require('connected-react-router/immutable');
import { createStructuredSelector } from 'reselect';
import { Dispatch } from 'redux';

import WorkspaceListItem from '../Common/WorkspaceListItem';

import Color from '../Common/Colors';
import { Workspace } from './Workspace';
import { filterWorkspaces } from './actions';
import { isFetchingWorkspaces, workspaceList } from './selectors';
import { CheckboxValueType } from 'antd/lib/checkbox/Group';

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

  behaviorChanged(checkedValue: CheckboxValueType[]) {
    const behavior: string[] = checkedValue.map(item => item.toString())
    this.setState({
      ...this.state,
      behavior
    });
    this.props.updateFilter(this.state.filter, behavior);
  }

  componentDidMount() {
    this.props.updateFilter('', []);
  }

  render() {
    const { fetching, workspaceList, openWorkspace } = this.props;
    const renderItem = (workspace: Workspace) => <WorkspaceListItem workspace={workspace} onSelected={openWorkspace} />
    return (
      <Row>
        <Col span={4}>
          <Card>
            <Input.Search
              onChange={this.filterUpdated}
              placeholder="find a workspace..."
              size="large"
            />
            <h3>Workspace Behavior</h3>
            <hr />
            <Checkbox.Group
              onChange={this.behaviorChanged}
              options={['Simple', 'Structured']} />
          </Card>
        </Col>
        <Col style={{ marginLeft: 25 }} span={20}>
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