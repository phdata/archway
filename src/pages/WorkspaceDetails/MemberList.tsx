import * as React from 'react';
import { Member } from '../../types/Workspace';
import { List } from 'antd';

interface Props {
  members: Member[];
}

const renderMember = (member: Member) => (
  <div>{member.name}</div>
);

const MemberList = ({ members }: Props) => (
  <List
    dataSource={members}
    renderItem={renderMember} />
);

export default MemberList;
