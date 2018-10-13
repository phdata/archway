import { Card, Row } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { Member } from '../../../types/Workspace';
import * as selectors from '../selectors';
import CardHeader from './CardHeader';

interface Props {
  members?: Member[];

  showModal: (e: React.MouseEvent) => void;
}

const renderMember = (member: Member) => (
  <div style={{ textAlign: 'center' }}>{member.name}</div>
);

const MemberList = ({ members, showModal }: Props) => (
  <Card
    actions={[
      <a href="#" onClick={showModal}>Add a member</a>,
    ]}>
    <CardHeader
      icon="lock"
      heading="Membership"
      subheading={`${members ? members!.length : 0} members`} />
    <Row gutter={12} type="flex" justify="center" style={{ marginTop: 18, flexDirection: 'column' }}>
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
