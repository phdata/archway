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
  Avatar
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

const MemberListItem = ({member, selected}) => (<List.Item style={{
    cursor: 'pointer',
    backgroundColor: selected
      ? '#B3B9C0'
      : 'white'
  }} actions={[<Icon type="right"/>]}>
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
</List.Item>);

class Members extends Component {
  componentDidMount() {
    this.props.getMembers();
  }

  render() {
    const {
      memberForm,
      existingMembers,
      newMembers,

      getMembers,
      memberFilterChanged,
      newMemberSelected,
      existingMemberSelected
    } = this.props;
    return (<Row>
      <Col span={8}>
        <div>
          <UsernameForm
            onChange={memberFilterChanged}
            memberForm={memberForm}/>
          {existingMembers && (
            <List
              bordered="bordered"
              dataSource={existingMembers}
              renderItem={item => <MemberListItem onSelect={newMemberSelected} member={item}/>}
              />
          )}
          {newMembers && (
            <List
              header={<h3> or add a new member ...</h3>}
              dataSource={newMembers}
              renderItem={item => <MemberListItem onSelect={existingMemberSelected} member={item}/>}
              />
          )}
        </div>
      </Col>
    </Row>);
  }
}

export default connect(
  state => state.workspaces.members,
  {
    getMembers,
    memberFilterChanged,
    existingMemberSelected,
    newMemberSelected
  })(Members);
