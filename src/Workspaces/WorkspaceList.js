import React from 'react';
import PropTypes from 'prop-types';
import { Button, Icon, List, Tabs, Avatar, Input } from 'antd';
import { connect } from 'react-redux';
import TimeAgo from 'react-timeago';
import { Link } from 'react-router-dom';

import { listWorkspaces } from './actions';

const WorkspaceItem = ({ item, onSelected }) => {
  const {
    id,
    name,
    requester,
    requested_date,
    single_user,
  } = item;
  return (
    <List.Item actions={[<Link to={`/workspaces/${id}`} type="default" onClick={() => onSelected(item)}>view</Link>]}>
      <List.Item.Meta
        avatar={<Avatar icon={single_user ? 'user' : 'team'} />}
        title={name}
        description={<div>by {requester} <TimeAgo date={requested_date} /></div>}
      />
    </List.Item>
  );
};

WorkspaceItem.propTypes = {
  item: PropTypes.objectOf({
    id: PropTypes.number.isRequired,
    name: PropTypes.string.isRequired,
    requester: PropTypes.string.isRequired,
    requested_date: PropTypes.string.isRequired,
    single_user: PropTypes.bool.isRequired,
  }).isRequired,
  onSelected: PropTypes.func.isRequired,
};

class WorkspaceList extends React.Component {
  componentDidMount() {
    this.props.listWorkspaces();
  }

  render() {
    const { fetching, onSelected, workspaceList } = this.props;
    return (
      <div>
        <Tabs tabPosition="left">
          <Tabs.TabPane tab={<Icon type="search" />}>
            <Input.Search
              placeholder="find a workspace..."
              size="large"
              onSearch={value => console.log(value)}
            />
            <List
              loading={fetching}
              dataSource={workspaceList}
              renderItem={item => <WorkspaceItem item={item} onSelected={onSelected} />}
            />
          </Tabs.TabPane>
        </Tabs>
        <div style={{ marginTop: 15 }}>
          <Button
            icon="plus"
            href="/request"
            type="primary"
            size="large"
            style={{ width: '100%' }}
          >
      Request a New Workspace
          </Button>
        </div>
      </div>
    );
  }
}

WorkspaceList.propTypes = {
  fetching: PropTypes.bool.isRequired,
  workspaceList: PropTypes.array,
  onSelected: PropTypes.func.isRequired,
};

export default connect(
  s => s.workspaces,
  {
    listWorkspaces,
  },
)(WorkspaceList);
