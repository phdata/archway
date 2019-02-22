import * as React from 'react';
import { Card, Table, Icon, Dropdown, Menu, Button } from 'antd';
import CardHeader from './CardHeader';
import { Member, HiveAllocation } from '../../../../models/Workspace';

interface Props {
  allocations: HiveAllocation[];
  members?: Member[];
  onAddMember: (e: React.MouseEvent) => void;
  onChangeMemberRole: (member: Member, id: number, role: string) => void;
}

const renderRoleColumn = (onChangeMemberRole: (member: Member, key: string, role: string) => void) =>
  ({ member, key }: { member: Member, key: string }) => {
    const memberRole = member.data[key] ? member.data[key].role : 'none';

    return (
      <Dropdown
        key={`${member.name}-${key}`}
        overlay={(
          <Menu
            onClick={({ key: role }) => {
              if (memberRole !== role) {
                onChangeMemberRole(member, key, role)
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

const PermissionsCard = ({ allocations, members, onAddMember, onChangeMemberRole }: Props) => {
  const roleKeys = allocations.map((allocation: HiveAllocation) => allocation.name);
  const roleColumns = {
    raw_loan_mecca: 'Raw',
    staging_loan_mecca: 'Staging',
    modeled_loan_mecca: 'Modeled',
  };
  const dataSource = members ? members.map((member: Member) => {
    const data = { name: member.name };
    roleKeys.forEach((key) => {
      data[key] = { member, key };
    });
    return data;
  }) : [];

  return (
    <Card style={{ height: '100%' }} bordered>
      <CardHeader>
        Permissions
        <Button style={{ marginLeft: 'auto' }} type="primary" onClick={onAddMember}>
          ADD
        </Button>
      </CardHeader>
      <Table
        dataSource={dataSource}
        pagination={false}
      >
        <Table.Column
          title="Name"
          dataIndex="name"
          key="name"
        />
        {roleKeys.map((roleKey: string) => (
          <Table.Column
            title={roleColumns[roleKey] || 'Default'}
            dataIndex={roleKey}
            key={roleKey}
            render={renderRoleColumn((member: Member, key: string, role: string) => {
              // convert key to id
              const filteredAllocations = allocations.filter((allocation: HiveAllocation) => allocation.name === key);
              if (filteredAllocations.length > 0) {
                onChangeMemberRole(member, filteredAllocations[0].id, role);
              }
            })}
          />
        ))}
      </Table>
    </Card>
  );
};

export default PermissionsCard;
