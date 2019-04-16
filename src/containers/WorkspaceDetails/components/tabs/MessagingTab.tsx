import * as React from 'react';
import { Tabs, Button, Row, Col } from 'antd';

import { TopicCard, TopicPermissionsCard } from '../cards';
import { Member, KafkaTopic, Workspace } from '../../../../models/Workspace';

interface Props {
  workspace?: Workspace;
  members?: Member[];
  selectedTopic?: KafkaTopic;
  onAddTopic: (e: React.MouseEvent) => void;
  onAddMember: (e: React.MouseEvent) => void;
  onChangeMemberRole: (distinguished_name: string, roleId: number, role: string, resource: string) => void;
  removeMember: (distinguished_name: string, database_role: string) => void;
}

class MessagingTab extends React.Component<Props> {
  public render() {
    const {
      workspace,
      members,
      onAddTopic,
      onAddMember,
      onChangeMemberRole,
    } = this.props;

    if (!workspace) {
      return null;
    }

    return (
      <div style={{ padding: 16 }}>
        <Button style={{ zIndex: 999, position: 'absolute', right: 12 }} type="primary" onClick={onAddTopic}>
          Add a Topic
        </Button>
        <Tabs>
          {workspace.topics.map((topic: KafkaTopic) => (
            <Tabs.TabPane tab={topic.name} key={topic.id.toString()}>
              <Row gutter={16} type="flex" justify="center">
                <Col span={24} style={{ marginBottom: 12 }}>
                  <TopicCard topic={topic} />
                </Col>
              </Row>
              <Row gutter={16} type="flex" justify="center">
                <Col span={24} style={{ marginBottom: 12 }}>
                  <TopicPermissionsCard
                    topic={topic}
                    members={members}
                    onAddMember={onAddMember}
                    onChangeMemberRole={(member, id, role) => {
                      onChangeMemberRole(member.distinguished_name, id, role, 'topics');
                    }}
                  />
                </Col>
              </Row>
            </Tabs.TabPane>
          ))}
        </Tabs>
      </div>
    );
  }
}

export default MessagingTab;
