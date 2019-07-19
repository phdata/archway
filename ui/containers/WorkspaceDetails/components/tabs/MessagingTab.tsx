import * as React from 'react';
import { Tabs, Button, Row, Col } from 'antd';

import { TopicCard, TopicPermissionsCard } from '../cards';
import { Member, KafkaTopic, Workspace } from '../../../../models/Workspace';
import { Profile } from '../../../../models/Profile';
import { ModalType } from '../../../../constants';

interface Props {
  workspace?: Workspace;
  profile: Profile;
  members?: Member[];
  selectedTopic?: KafkaTopic;
  showModal: (e: React.MouseEvent, type: ModalType) => void;
  onChangeMemberRole: (distinguished_name: string, roleId: number, role: string, resource: string) => void;
  removeMember: (distinguished_name: string, roleId: number, resource: string) => void;
}

class MessagingTab extends React.Component<Props> {
  public render() {
    const { workspace, profile, members, showModal, onChangeMemberRole, removeMember } = this.props;

    if (!workspace) {
      return null;
    }

    const isLiaison = workspace.requester === profile.distinguished_name;
    let currentMember: Member;
    const filteredMembers =
      members && members.filter((member: Member) => member.distinguished_name === profile.distinguished_name);
    if (filteredMembers && filteredMembers.length > 0) {
      currentMember = filteredMembers[0];
    }

    return (
      <div style={{ padding: 16 }}>
        <Button
          style={{ zIndex: 999, position: 'absolute', right: 12 }}
          type="primary"
          onClick={e => showModal(e, ModalType.Kafka)}
        >
          Add a Topic
        </Button>
        <Tabs>
          {workspace.topics.map((topic: KafkaTopic) => {
            const roleData = currentMember && currentMember.topics[topic.name];
            const roleValue = roleData && roleData.role;
            const hasPermission = isLiaison || roleValue === 'manager';

            return (
              <Tabs.TabPane tab={topic.name} key={topic.id.toString()}>
                <Row gutter={16} type="flex" justify="center">
                  <Col span={24} style={{ marginBottom: 12 }}>
                    <TopicCard topic={topic} />
                  </Col>
                </Row>
                <Row gutter={16} type="flex" justify="center">
                  <Col span={24} style={{ marginBottom: 12 }}>
                    <TopicPermissionsCard
                      readonly={!hasPermission}
                      topic={topic}
                      members={members}
                      onAddMember={e => showModal(e, ModalType.SimpleTopicMember)}
                      onChangeMemberRole={(member, id, role) => {
                        onChangeMemberRole(member.distinguished_name, id, role, 'topics');
                      }}
                      removeMember={removeMember}
                    />
                  </Col>
                </Row>
              </Tabs.TabPane>
            );
          })}
        </Tabs>
      </div>
    );
  }
}

export default MessagingTab;
