import { Card, Icon, List } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { Member } from '../../../types/Workspace';
import * as selectors from '../selectors';
import Label from './Label';

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
    <Label style={{ lineHeight: '18px' }}>
      <Icon type="team" style={{ paddingRight: 5, fontSize: 18 }} />Members
    </Label>
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
