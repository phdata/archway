import React from 'react';
import { List, Row, Col, Form, Select, Input, Avatar, Icon, Button, Popconfirm } from 'antd';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import ValueDisplay from './ValueDisplay';
import { addMember, removeMember, newMemberFormChanged } from './actions';

const UserForm = Form.create({
  onFieldsChange(props, changedFields) {
    props.onChange(changedFields);
  },
  mapPropsToFields(props) {
    return {
      username: Form.createFormField({
        value: props.newMemberForm.username,
      }),
      role: Form.createFormField({
        value: props.newMemberForm.role,
      }),
    };
  },
})(({
  form: {
    getFieldDecorator,
  },
  addMember,
  addingUser,
}) => {
  const RoleSelect = getFieldDecorator('role', {})(<Select>
    <Select.Option value="manager">Read/Write</Select.Option>
  </Select>);
  return (
    <Form
        onSubmit={(e) => {
          e.preventDefault();
          addMember();
        }}
        layout="horizontal"
      >
      <Form.Item>
        {getFieldDecorator('username', { rules: [{ required: true }] })(
            <Input addonAfter={RoleSelect} placeholder="username" />
        )}
       <div style={{ color: '#aaa', fontSize: 12 }}>
          <Icon type="info-circle-o" /> enter a username and hit enter to add
      </div>
      </Form.Item>
    </Form>
  );
});


const ListHeader = ({ name }) => (
  <h3 style={{ textAlign: 'center' }}>
    {name}
  </h3>
);

const ListItem = ({ member: { username, role }, removeMember }) => {
  const avatar = role === 'readonly' ? 'RO' : 'RW';
  return (
    <List.Item>
      <List.Item.Meta
        style={{ alignItems: 'center' }}
        avatar={<Avatar style={{ backgroundColor: '#D7C9AA' }}>{avatar}</Avatar>}
        title={username}
      />
      <Popconfirm title={`Are you sure you want to remove ${username}?`} onConfirm={_ => removeMember(username, role)} >
        <Button size="small" icon="minus" shape="circle" style={{ backgroundColor: '#7B2D26', color: '#F0F3F5' }} />
      </Popconfirm>
    </List.Item>
  );
};

class DBDisplay extends React.Component {
  render() {
    const {
      database: {
        name,
        size_in_gb,
      },
      members: {
        managers,
        readonly,
      },
      addMember,
      newMemberForm,
      newMemberFormChanged,
      removeMember,
    } = this.props;
  const managerList = (managers && managers.map(member => ({ ...member, role: 'manager' }))) || [];
  const readonlyList = (readonly && readonly.map(member => ({ ...member, role: 'readonly' }))) || [];
  return (
    <div>
      <Row className="Data" type="flex" align="center">
        <Col span={18} style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-around' }}>
          <ValueDisplay label="database name">
            {name}
          </ValueDisplay>
          <ValueDisplay label="disk quota">
            {`${size_in_gb}gb`}
          </ValueDisplay>
        </Col>
        <Col span={6}>
          <List
            header={<ListHeader name="Workspace Managers" />}
            footer={<UserForm onChange={newMemberFormChanged} addMember={addMember} addingUser={false} newMemberForm={newMemberForm} />}
            dataSource={managerList.concat(readonlyList)}
            renderItem={item => <ListItem member={item} removeMember={removeMember} />}
          />
        </Col>
      </Row>
    </div>
  );
  }
}

DBDisplay.propTypes = {
  database: PropTypes.shape({
    name: PropTypes.string.isRequired,
    size_in_gb: PropTypes.number.isRequired,
  }),
  members: PropTypes.shape({
    managers: PropTypes.arr,
    readonly: PropTypes.arr,
  }),
  addMember: PropTypes.func,
  newMemberForm: PropTypes.obj,
  newMemberFormChanged: PropTypes.func,
  removeMember: PropTypes.func,
};

export default connect(
  state => state.workspaces,
  { 
    addMember, 
    newMemberFormChanged,
    removeMember,
  }
)(DBDisplay);
