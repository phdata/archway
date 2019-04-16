import * as React from 'react';
import { Card, Table, Icon, Dropdown, Menu, Button } from 'antd';
import CardHeader from './CardHeader';
import { Member, HiveAllocation } from '../../../../models/Workspace';

interface Props {
  allocation: HiveAllocation;
  members?: Member[];
  onAddMember: (e: React.MouseEvent) => void;
  onChangeMemberRole: (member: Member, id: number, role: string) => void;
}

const renderRoleColumn = (key: string, onChangeMemberRole: (member: Member, role: string) => void) =>
  (member: any) => {
    const memberRole = member.data[key] ? member.data[key].role : 'none';

    return (
      <Dropdown
        key={`${member.name}-${key}`}
        overlay={(
          <Menu
            onClick={({ key: role }) => {
              if (memberRole !== role) {
                onChangeMemberRole(member, role)
              }
            }}
          >
            <Menu.Item key="none">none</Menu.Item>
            <Menu.Item key="readonly">readonly</Menu.Item>
            <Menu.Item key="manager">manager</Menu.Item>
          </Menu>
        )}
        trigger={['click']}
      >
        <a style={{ fontSize: 12 }} href="#">
          {memberRole}
          <Icon style={{ marginLeft: 4 }} type="down" />
        </a>
      </Dropdown>
    );
  };

const PermissionsCard = ({ allocation, members, onAddMember, onChangeMemberRole }: Props) => (
  <Card style={{ height: '100%' }} bordered>
    <CardHeader>
      Permissions
      <Button style={{ marginLeft: 'auto' }} type="primary" onClick={onAddMember}>
        Add a Member
      </Button>
    </CardHeader>
    <Table
      dataSource={members}
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
        render={renderRoleColumn(allocation.name, (member: Member, role: string) => {
          onChangeMemberRole(member, allocation.id, role);
        })}
      />
      ))}
    </Table>
  </Card>
);

export default PermissionsCard;
