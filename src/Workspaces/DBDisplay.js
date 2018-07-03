import React from 'react';
import { List, Row, Col, Form, Select, Input, Avatar, Icon } from 'antd';
import PropTypes from 'prop-types';

import ValueDisplay from './ValueDisplay';

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

const ListFooter = ({ newMemberForm, addMember, onChange }) => (
  <div>
    <UserForm onChange={onChange} addMember={addMember} addingUser={false} newMemberForm={newMemberForm} />
  </div>
);

const ListHeader = ({ name }) => (
  <h3 style={{ textAlign: 'center' }}>
    {name}
  </h3>
);

const ListItem = ({ username, role }) => {
  const avatar = role === 'readonly' ? 'RO' : 'RW';
  return (
    <List.Item>
      <List.Item.Meta
        style={{ alignItems: 'center' }}
        avatar={<Avatar style={{ backgroundColor: '#D7C9AA' }}>{avatar}</Avatar>}
        title={username}
      />
    </List.Item>
  );
};

const DBDisplay = ({
  name,
  size_in_gb,
  managers,
  readonly,
  addMember,
  newMemberForm,
  newMemberFormChanged,
}) => {
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
            footer={<ListFooter onChange={newMemberFormChanged} newMemberForm={newMemberForm} addMember={addMember} />}
            dataSource={managerList.concat(readonlyList)}
            renderItem={ListItem}
          />
        </Col>
      </Row>
    </div>
  );
};

DBDisplay.propTypes = {
  name: PropTypes.string.isRequired,
  size_in_gb: PropTypes.number.isRequired,
  managers: PropTypes.arr,
  readonly: PropTypes.arr,
  addMember: PropTypes.func,
  newMemberForm: PropTypes.obj,
  newMemberFormChanged: PropTypes.func,
};

export default DBDisplay;
