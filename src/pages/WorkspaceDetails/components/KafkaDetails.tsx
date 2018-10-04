import { Card, Icon, List } from 'antd';
import * as React from 'react';
import { KafkaTopic } from '../../../types/Workspace';
import Label from './Label';
import Colors from '../../../components/Colors';

interface Props {
    consumerGroup: string;
    topics: KafkaTopic[];

    showModal: (e: React.MouseEvent) => void;
}

const renderTopic = (topic: KafkaTopic) => (
  <div>topic.name</div>
);

const KafkaDetails = ({ consumerGroup, topics, showModal }: Props) => (
  <Card
    actions={[
      <a href="#" onClick={showModal}>Add a topic</a>,
    ]}>
    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
      <Label style={{ lineHeight: '20px' }}>
        <Icon
          theme="twoTone"
          twoToneColor={Colors.Green.string()}
          type="sound"
          style={{ paddingRight: 5, fontSize: 20 }} />Kafka
      </Label>
      <Label style={{ lineHeight: '18px', fontSize: 10 }}>
        {consumerGroup}
      </Label>
    </div>
    <List
      dataSource={topics}
      renderItem={renderTopic}
      locale={{ emptyText: 'No topics yet' }} />
  </Card>
);

export default KafkaDetails;
