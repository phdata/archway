import * as React from 'react';
import { Button, Checkbox, Row, Col, List, Avatar, Input, Form, Card, Tag } from 'antd';
import { connect } from 'react-redux';
import { push } from 'react-router-redux'
import { createStructuredSelector } from 'reselect';
import { Dispatch } from 'redux';

import WorkspaceListItem from '../Common/WorkspaceListItem';

import Color from '../Common/Colors';
import { Workspace } from './Workspace';
import { filterWorkspaces } from './actions';
import { isFetchingWorkspaces, workspaceList } from './selectors';

interface Props {
  fetching: boolean
  workspaceList: Array<Workspace>
  updateFilter: (filter: string, behavior: string) => void
  openWorkspace: (id: number) => void
}

interface State {
  filter: string
  behavior: String
}

class WorkspaceList extends React.Component<Props, State> {
  filterUpdated(filter: string) {
    this.setState({
      ...this.state,
      filter
    });
  }

  behaviorChanged(behavior: string) {
    this.setState({
      ...this.state,
      behavior
    });
  }

  componentDidMount() {
    this.props.updateFilter('', '');
  }

  render() {
    const { fetching, workspaceList, openWorkspace } = this.props;
    const renderItem = (workspace: Workspace) => <WorkspaceListItem workspace={workspace} onSelected={openWorkspace} />
    return (
      <Row>
        <Col span={4}>
          <Card>
            <Input.Search
              placeholder="find a workspace..."
              size="large"
            />
            <h3>Workspace Behavior</h3>
            <hr />
            <Checkbox.Group options={['Simple', 'Structured']} />
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
  openWorkspace: (id: number) => dispatch(push(`/workspaces/${id}`)),
  updateFilter: (filter: string, behavior: string) => dispatch(filterWorkspaces(filter, behavior))
});

export default connect(mapStateToProps, mapDispatchToProps)(WorkspaceList);