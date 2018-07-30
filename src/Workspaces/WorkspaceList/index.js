import React from 'react';
import PropTypes from 'prop-types';
import { Button, Icon, List, Tabs, Avatar, Input, Form } from 'antd';
import { connect } from 'react-redux';
import TimeAgo from 'react-timeago';
import { push } from 'react-router-redux'

import { listWorkspaces, filterChanged } from './actions';

const SearchForm = Form.create({
  onFieldsChange(props, changedFields) {
    props.onChange(changedFields);
  },
  mapPropsToFields(props) {
    return {
      filter: Form.createFormField({
        value: props.searchForm.filter,
      }),
    };
  },
})(({
  form: {
    getFieldDecorator,
  },
}) => (
  <Form onSubmit={(e) => {e.preventDefault()}}>
      <Form.Item>
        {getFieldDecorator('filter', {})(
          <Input.Search
            placeholder="find a workspace..."
            size="large"
          />
        )}
      </Form.Item>
  </Form>
));

const WorkspaceItem = ({ item, onSelected }) => {
  const {
    id,
    name,
    requester,
    requested_date,
    single_user,
  } = item;
  return (
    <List.Item actions={[<Button icon="search" type="primary" onClick={() => onSelected(`/workspaces/${id}`)}>view</Button>]}>
      <List.Item.Meta
        avatar={<Avatar icon={single_user ? 'user' : 'team'} />}
        title={name}
        description={<div>by {requester} <TimeAgo date={requested_date} /></div>}
      />
    </List.Item>
  );
};

WorkspaceItem.propTypes = {
  item: PropTypes.shape ({
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
    const { fetching, filteredList, searchForm, filterChanged, push } = this.props;
    return (
      <div>
        <SearchForm onChange={filterChanged} searchForm={searchForm} />
        <List
          loading={fetching}
          dataSource={filteredList}
          renderItem={item => <WorkspaceItem item={item} onSelected={push} />}
        />
      </div>
    );
  }
}

WorkspaceList.propTypes = {
  fetching: PropTypes.bool.isRequired,
  filteredList: PropTypes.array,
  searchForm: PropTypes.object,
  filterChanged: PropTypes.func.isRequired,
};

export default connect(
  s => s.workspaces.listing,
  {
    listWorkspaces,
    filterChanged,
    push,
  },
)(WorkspaceList);
