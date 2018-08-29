import React from 'react';
import PropTypes from 'prop-types';
import { Button, Checkbox, Row, Col, List, Avatar, Input, Form, Card, Tag } from 'antd';
import { connect } from 'react-redux';
import TimeAgo from 'react-timeago';
import { push } from 'react-router-redux'

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
    onSubmit={(e) => {e.preventDefault()}}>
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
        {getFieldDecorator('simple', {
          style: { marginBottom: 0 }
        })(
          <Checkbox.Group options={[ 'Simple', 'Structured' ]} />
        )}
      </Form.Item>
      <Button type="primary" style={{ width: '100%' }} onClick={() => {}}>Filter</Button>
  </Form>
));

const WorkspaceStatus = () => (
  <div
    style={{
      width: '50%',
      backgroundColor: 'green',
      display: 'inline-block',
      color: 'white',
      marginTop: 5
    }}>
    Approved
  </div>
);

const detailStyle = {
  width: '25%',
  textAlign: 'center',
  boxShadow: 'none'
};

const WorkspaceDetail = ({ label, value }) => (
  <Card.Grid style={detailStyle}>
    <h2 style={{ color: Color.Orange.rgb().string(), fontWeight: 100, textAlign: 'center' }}>
      {value}
    </h2>
    <h5 style={{ textAlign: 'center' }}>
      {label}
    </h5>
  </Card.Grid>
)

const WorkspaceItem = ({ item, onSelected }) => {
  const gridStyle = { width: '33%', textAlign: 'center' };
  const {
    id,
    name,
    requester,
    requested_date,
    single_user,
  } = item;
  return (
    <List.Item>
      <Card
        style={{ textAlign: 'center' }}
        onClick={() => onSelected(`/workspaces/${id}`)}
        hoverable>
        <Avatar icon="user" />
        <h2 style={{ textAlign: 'center' }}>{name}</h2>
        <h4>partially approved</h4>
        <WorkspaceDetail label="DBs" value={1} />
        <WorkspaceDetail label="Pools" value={1} />
        <WorkspaceDetail label="Topics" value={0} />
        <WorkspaceDetail label="Apps" value={1} />
      </Card>
    </List.Item>
  );
};

WorkspaceItem.propTypes = {
  item: PropTypes.shape({
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
            renderItem={item => <WorkspaceItem item={item} onSelected={push} />}
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