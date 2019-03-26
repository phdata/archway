import * as React from 'react';
import { Row, Col, Button } from 'antd';

import { TopicCard, TopicPermissionsCard } from '../cards';
import { Member, KafkaTopic, Workspace } from '../../../../models/Workspace';

interface Props {
  workspace?: Workspace;
  members?: Member[];
  selectedTopic?: KafkaTopic;
  onAddTopic: (e: React.MouseEvent) => void;
  onAddMember: (e: React.MouseEvent) => void;
  onChangeMemberRole: (distinguished_name: string, roleId: number, role: string, resource: string) => void;
  onSelectTopic: (topic: KafkaTopic) => void;
  removeMember: (distinguished_name: string, database_role: string) => void;
}

class MessagingTab extends React.Component<Props> {
  public render() {
    const {
      workspace,
      members,
      selectedTopic,
      onAddTopic,
      onAddMember,
      onChangeMemberRole,
      onSelectTopic,
    } = this.props;

    if (!workspace) {
      return null;
    }

    return (
      <div style={{ padding: 16 }}>
        <Row style={{ height: 'calc(100vh - 465px)' }} gutter={16} type="flex">
          <Col span={8} style={{ display: 'flex', flexDirection: 'column', maxHeight: '100%' }}>
            <Button style={{ alignSelf: 'flex-start', marginBottom: 16 }} type="primary" onClick={onAddTopic}>
              Add a Topic
            </Button>
            <div style={{ flex: 1, overflow: 'auto' }}>
              {workspace.topics.map((topic: KafkaTopic) => (
                <TopicCard
                  key={topic.id}
                  topic={topic}
                  selected={selectedTopic && selectedTopic.id === topic.id}
                  onClick={() => onSelectTopic(topic)}
                />
              ))}
            </div>
          </Col>
          <Col span={16}>
            {selectedTopic && (<TopicPermissionsCard
              topic={selectedTopic}
              members={members}
              onAddMember={onAddMember}
              onChangeMemberRole={(member, id, role) => {
                onChangeMemberRole(member.distinguished_name, id, role, 'topics');
              }}
            />)}
          </Col>
        </Row>
      </div>
    );
  }
}

export default MessagingTab;
