import React, { Component } from 'react';
import {connect} from 'react-redux';
import {
  Spin,
  Row,
  Col,
  Icon,
  Button,
  Tabs,
  Tag,
  Menu,
  List,
  Input,
  Form,
  Avatar,
  Select,
} from 'antd';

import {
  getMembers,
  memberFilterChanged,
  existingMemberSelected,
  newMemberSelected,
} from './actions';

const UsernameForm = Form.create({
  onFieldsChange(props, changedFields) {
    props.onChange(changedFields);
  },
  mapPropsToFields(props) {
    return {
      filter: Form.createFormField({value: props.memberForm.filter})
    };
  }
})(({form: {
    getFieldDecorator
  }}) => (<Form onSubmit={(e) => {
      e.preventDefault()
    }}>
    <Form.Item>
      {getFieldDecorator('filter', {})(<Input.Search placeholder="add or find a member..." size="large"/>)}
    </Form.Item>
  </Form>));

const MemberDetails = ({workspace, member}) => (
  <div>
    <h3>Data</h3>
    <hr />
    {workspace.data.map(ds => (
      <div>
        {ds.name}: <Select value={member.data[ds.name] ? member.data[ds.name].role : "none"}>
        <Select.Option value="none">None</Select.Option>
        <Select.Option value="readonly">Read Only</Select.Option>
        <Select.Option value="manager">Manager</Select.Option>
      </Select>
      </div>
    ))}
    <h3>Topics</h3>
    <hr />
    {workspace.topics.map(ds => (
      <div>
        {ds.name}: <Select value={member.topics[ds.name] ? member.topics[ds.name].role : "none"}>
        <Select.Option value="none">None</Select.Option>
        <Select.Option value="readonly">Read Only</Select.Option>
        <Select.Option value="manager">Manager</Select.Option>
      </Select>
      </div>
    ))}
    <h3>Applications</h3>
    <hr />
    {workspace.applications.map(ds => (
      <div>
        {ds.name}: <Select value={member.applications[ds.name] ? member.applications[ds.name].role : "none"}>
        <Select.Option value="none">None</Select.Option>
        <Select.Option value="readonly">Read Only</Select.Option>
        <Select.Option value="manager">Manager</Select.Option>
      </Select>
      </div>
    ))}
  </div>
)

const MemberListItem = ({member, selected, onSelect}) => (
  <List.Item
    onClick={() => onSelect(member)}
    style={{
      cursor: 'pointer',
      backgroundColor: selected
        ? '#B3B9C0'
        : 'white'
    }}
    actions={[<Icon type="right"/>]}>
    <div style={{
        display: 'flex',
        alignItems: 'center'
      }}>
      <Avatar icon="user"/>
      <h3 style={{
          marginLeft: 10,
          marginBottom: 0
        }}>{member.username}</h3>
    </div>
  </List.Item>
);

class Members extends Component {
  componentDidMount() {
    this.props.getMembers();
  }

  render() {
    const {
      activeWorkspace,

      memberForm,
      existingMembers,
      newMembers,
      selectedUser,

      getMembers,
      memberFilterChanged,
      newMemberSelected,
      existingMemberSelected
    } = this.props;
    return (<Row gutter={12}>
      <Col span={8}>
          <UsernameForm
            onChange={memberFilterChanged}
            memberForm={memberForm}/>
          {existingMembers && (
            <List
              bordered="bordered"
              dataSource={existingMembers}
              renderItem={item => <MemberListItem onSelect={existingMemberSelected} member={item} selected={item === selectedUser} />}
              />
          )}
          {newMembers && (
            <List
              header={<h3> or add a new member ...</h3>}
              dataSource={newMembers}
              renderItem={item => <MemberListItem onSelect={newMemberSelected} member={item}/>}
              />
          )}
      </Col>
      <Col span={16}>
        {selectedUser && <MemberDetails workspace={activeWorkspace} member={selectedUser} />}
      </Col>
    </Row>);
  }
}

export default connect(
  state => ({
    ...state.workspaces.members,
    activeWorkspace: state.workspaces.details.activeWorkspace,
  }),
  {
    getMembers,
    memberFilterChanged,
    existingMemberSelected,
    newMemberSelected
  })(Members);
