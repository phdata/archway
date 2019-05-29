import * as React from 'react';
import { Card, Table, Icon, Dropdown, Menu, Button } from 'antd';
import CardHeader from './CardHeader';
import { Member, HiveAllocation } from '../../../../models/Workspace';

interface Props {
  readonly: boolean;
  allocation: HiveAllocation;
  members?: Member[];
  onAddMember: (e: React.MouseEvent) => void;
  onChangeMemberRole: (member: Member, id: number, role: string) => void;
  removeMember: (distinguished_name: string, roleId: number, resource: string) => void;
}

const renderRoleColumn = (key: string, onChangeMemberRole?: (member: Member, role: string) => void) =>
  (member: any) => (
    <Dropdown
      key={`${member.name}-${key}`}
      disabled={!onChangeMemberRole}
      overlay={(
        <Menu
          onClick={({ key: role }) => {
            if (member.data[key].role !== role && onChangeMemberRole) {
              onChangeMemberRole(member, role)
            }
          }}
        >
          <Menu.Item key="readonly">readonly</Menu.Item>
          <Menu.Item key="readwrite">read/write</Menu.Item>
          <Menu.Item key="manager">manager</Menu.Item>
        </Menu>
      )}
      trigger={['click']}
    >
      <a style={{ fontSize: 12 }} href="#"> {/* eslint-disable-line */}
        {member.data[key].role}
        <Icon style={{ marginLeft: 4 }} type="down" />
      </a>
    </Dropdown>
  );

const PermissionsCard = ({ readonly, allocation, members, onAddMember, onChangeMemberRole, removeMember }: Props) => (
  <Card style={{ height: '100%' }} bordered>
    <CardHeader>
      Permissions
      {!readonly && (
        <Button style={{ marginLeft: 'auto' }} type="primary" onClick={onAddMember}>
          Add a Member
        </Button>
      )}
    </CardHeader>
    <Table
      dataSource={members && members.filter(m => m.data.hasOwnProperty(allocation.name))}
      pagination={false}
      rowKey="distinguished_name"
    >
      <Table.Column
        title="Name"
        dataIndex="name"
        key="name"
      />
      <Table.Column
        title="Role"
        render={renderRoleColumn(allocation.name, readonly ? undefined : (member: Member, role: string) => {
          onChangeMemberRole(member, allocation.id, role);
        })}
      />
      <Table.Column
        title="Remove"
        render={member => (readonly ? <span /> : <Button type="danger" shape="circle" icon="delete" onClick={_ => removeMember(member.distinguished_name, allocation.id, 'data')} />)}
      />
    </Table>
  </Card>
);

export default PermissionsCard;
