import * as React from 'react';
import { Card, Table, Icon, Dropdown, Menu, Button } from 'antd';
import CardHeader from './CardHeader';
import { Member, KafkaTopic } from '../../../../models/Workspace';

interface Props {
  readonly: boolean;
  topic?: KafkaTopic;
  members?: Member[];
  onAddMember: (e: React.MouseEvent) => void;
  onChangeMemberRole: (member: Member, id: number, role: string) => void;
}

const renderRoleColumn = (onChangeMemberRole?: (member: Member, id: number, role: string) => void) =>
  ({ member, role, roleId }: { member: Member, role: string, roleId: number }) => {
    return (
      <Dropdown
        key={member.distinguished_name}
        overlay={(
          <Menu
            onClick={({ key: newRole }) => {
              if (newRole !== role && onChangeMemberRole) {
                onChangeMemberRole(member, roleId, role)
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
          {role}
          <Icon style={{ marginLeft: 4 }} type="down" />
        </a>
      </Dropdown>
    );
  };

const TopicPermissionsCard = ({ readonly, topic, members, onAddMember, onChangeMemberRole }: Props) => {
  const dataSource = members && topic
    ? members.filter((member: Member) => !!member.topics[topic.name]).map((member: Member) => ({
      name: member.name,
      permission: {
        member,
        role: member.topics[topic.name].role,
        roleId: member.topics[topic.name].id,
      },
    }))
    : [];

  return (
    <Card style={{ height: '100%' }} bordered>
      <CardHeader>
        Permissions
        {!!topic && !readonly && (
          <Button style={{ marginLeft: 'auto' }} type="primary" onClick={onAddMember}>
            Add a Member
          </Button>
        )}
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
        <Table.Column
          title="PERMISSIONS"
          dataIndex="permission"
          render={renderRoleColumn(readonly ? undefined : onChangeMemberRole)}
        />
      </Table>
    </Card>
  );
};

export default TopicPermissionsCard;
