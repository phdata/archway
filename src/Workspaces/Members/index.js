import React, { Component } from 'react';
import { connect } from 'react-redux';
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
  roleChanged,
} from './actions';

const UsernameForm = Form.create({
  onFieldsChange(props, changedFields) {
    props.onChange(changedFields);
  },
  mapPropsToFields(props) {
    return {
      filter: Form.createFormField({ value: props.memberForm.filter })
    };
  }
})(({
  form: {
    getFieldDecorator
  }
}) => (<Form onSubmit={e => e.preventDefault()}>
    <Form.Item>
      {getFieldDecorator('filter', {})(<Input.Search placeholder="add or find a member..." size="large"/>)}
    </Form.Item>
  </Form>));

const MemberRole = ({ member, area, workspaceArea, readonlyAlso, roleSet, roleChanged }) => (
  <div style={{ display: 'flex', flexDirection: 'row', alignItems: 'center', marginBottom: 25 }}>
    <div style={{ marginRight: 15 }}>
      {workspaceArea.name}
    </div>
    <Select
      value={member[area] && member[area][workspaceArea.name] ? member[area][workspaceArea.name].role : "none"}
      onSelect={newRole => {
        const oldRole = member[area] && member[area][workspaceArea.name] && member[area][workspaceArea.name].role
        roleChanged(member.username, area, workspaceArea.id, oldRole, newRole)
      }}>
      <Select.Option value="none">None</Select.Option>
      {readonlyAlso && <Select.Option value="readonly">Read Only</Select.Option>}
      <Select.Option value="manager">Manager</Select.Option>
    </Select>
  </div>
);

const WorkspaceArea = ({ roleChanged, label, workspace, member, area, readonlyAlso = false }) => (
  <div>
    <h3>{label}</h3>
    <hr />
    {workspace[area].length == 0 && (
      <div style={{ marginBottom: 25 }}>
        Nothing here yet...
      </div>
    )}
    {workspace[area].map(i => (
      <MemberRole
        member={member}
        area={area}
        workspaceArea={i}
        roleChanged={roleChanged}
        readonlyAlso={readonlyAlso} />
    ))}
  </div>
);

const MemberDetails = ({ roleChanged, workspace, member }) => (
  <div>
    <WorkspaceArea
      label="Database Access"
      roleChanged={roleChanged}
      workspace={workspace}
      member={member}
      area="data"
      readonlyAlso />
    <WorkspaceArea
      label="Topic Access"
      roleChanged={roleChanged}
      workspace={workspace}
      member={member}
      area="topics"
      readonlyAlso />
    <WorkspaceArea
      label="Application/Consumer Group Access"
      roleChanged={roleChanged}
      workspace={workspace}
      member={member}
      area="applications" />
  </div>
)

const MemberListItem = ({ member, selected, onSelect, icon = 'user' }) => (
  <List.Item
    onClick={() => onSelect(member)}
    style={{
      cursor: 'pointer',
      backgroundColor: selected
        ? '#D9DCDF'
        : 'white'
    }}
    actions={[<Icon type="right"/>]}>
    <div style={{
        display: 'flex',
        alignItems: 'center'
      }}>
      <Avatar icon={icon}/>
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
      filteredMembers,
      newMembers,
      selectedUser,

      getMembers,
      memberFilterChanged,
      newMemberSelected,
      existingMemberSelected,
      roleChanged,
    } = this.props;
    return (<Row gutter={12}>
      <Col span={6}>
          <UsernameForm
            onChange={memberFilterChanged}
            memberForm={memberForm}/>
          {filteredMembers && (
            <List
              bordered
              locale={{ emptyText: 'No existing members match' }}
              dataSource={filteredMembers}
              renderItem={item => <MemberListItem onSelect={existingMemberSelected} member={item} selected={selectedUser && selectedUser.username === item.username} />}
              />
          )}
          {newMembers && (
            <List
              bordered
              dataSource={newMembers}
              renderItem={item => <MemberListItem onSelect={existingMemberSelected} icon="plus" selected={selectedUser && selectedUser.username === item.username} member={item}/>}
              />
          )}
      </Col>
      <Col span={17} offset={1}>
        {selectedUser && <MemberDetails workspace={activeWorkspace} member={selectedUser} roleChanged={roleChanged} />}
      </Col>
    </Row>);
  }
}

export default connect(
  state => ({
    ...state.workspaces.members,
    activeWorkspace: state.workspaces.details.activeWorkspace,
  }), {
    getMembers,
    memberFilterChanged,
    existingMemberSelected,
    newMemberSelected,
    roleChanged
  })(Members);
