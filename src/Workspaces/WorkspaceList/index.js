import React from 'react';
import PropTypes from 'prop-types';
import { Button, Checkbox, Row, Col, List, Avatar, Input, Form, Card, Tag } from 'antd';
import { connect } from 'react-redux';
import TimeAgo from 'react-timeago';
import { push } from 'react-router-redux'

import WorkspaceListItem from '../../Common/WorkspaceListItem';

import { listWorkspaces, filterChanged } from './actions';

import Color from '../../Common/Colors';

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
  <Form
    layout="vertical"
    onSubmit={e => e.preventDefault()}>
      <Form.Item label="Search">
        {getFieldDecorator('filter', {})(
          <Input.Search
            placeholder="find a workspace..."
            size="large"
          />
        )}
      </Form.Item>
      <h3>Workspace Type</h3>
      <hr />
      <Form.Item>
        {getFieldDecorator('workspaceType', {
          style: { marginBottom: 0 }
        })(
          <Checkbox.Group options={[ 'Simple', 'Structured' ]} />
        )}
      </Form.Item>
      <Button type="primary" style={{ width: '100%' }} onClick={() => {}}>Filter</Button>
  </Form>
));

class WorkspaceList extends React.Component {
  componentDidMount() {
    this.props.listWorkspaces();
  }

  render() {
    const { fetching, filteredList, searchForm, filterChanged, push } = this.props;
    return (
      <Row>
        <Col span={4}>
          <Card>
            <SearchForm onChange={filterChanged} searchForm={searchForm} />
          </Card>
        </Col>
        <Col span={20}>
          <List
            style={{ marginLeft: 25 }}
            grid={{ gutter: 16, column: 4 }}
            locale={{ emptyText: 'No workspaces yet. Create one from the link on the left.' }}
            loading={fetching}
            dataSource={filteredList}
            renderItem={item => <WorkspaceListItem workspace={item} onSelected={push} />}
          />
        </Col>
      </Row>
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
  s => s.workspaces.listing, {
    listWorkspaces,
    filterChanged,
    push,
  },
)(WorkspaceList);