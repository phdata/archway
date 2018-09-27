import { Card, Icon, List } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { Member } from '../../../types/Workspace';
import * as selectors from '../selectors';
import Label from './Label';
import Colors from '../../../components/Colors';

interface Props {
  members?: Member[];
}

const renderMember = (member: Member) => (
  <div>{member.name}</div>
);

const MemberList = ({ members }: Props) => (
  <Card
    actions={[
      <a href="#">Add a member</a>,
    ]}>
    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
      <Label style={{ lineHeight: '20px', justifyContent: 'start' }}>
        <Icon theme="twoTone" twoToneColor={Colors.Green.string()} type="lock" style={{ paddingRight: 5, fontSize: 20 }} />Members
      </Label>
      <Label style={{ lineHeight: '18px' }}>
        <small>{members ? members.length : 0} members</small>
      </Label>
    </div>
    <List
      dataSource={members}
      renderItem={renderMember} />
  </Card>
);

const mapStateToProps = () =>
  createStructuredSelector({
    members: selectors.getMembers(),
  });

const mapDispatchToProps = () => ({});

export default connect(mapStateToProps, mapDispatchToProps)(MemberList);
