import { Card, Row } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { Member } from '../../../types/Workspace';
import * as selectors from '../selectors';
import CardHeader from './CardHeader';

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
    <CardHeader
      icon="lock"
      heading="Membership"
      subheading={`${members ? members!.length : 0} members`} />
    <Row gutter={12} type="flex" justify="center" style={{ marginTop: 18 }}>
      {members && members.length > 0 && members.map(renderMember)}
      {(!members || members.length <= 0) && (
        <div style={{ color: 'rgba(0, 0, 0, .65)' }}>
          No additional members yet.
        </div>
      )}
    </Row>
  </Card>
);

const mapStateToProps = () =>
  createStructuredSelector({
    members: selectors.getMembers(),
  });

const mapDispatchToProps = () => ({});

export default connect(mapStateToProps, mapDispatchToProps)(MemberList);
