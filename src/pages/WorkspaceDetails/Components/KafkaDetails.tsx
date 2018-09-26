import { Card, Icon, List } from 'antd';
import * as React from 'react';
import { KafkaTopic } from '../../../types/Workspace';
import Label from './Label';

interface Props {
  consumerGroup: string;
  topics: KafkaTopic[];
}

const renderTopic = (topic: KafkaTopic) => (
  <div>topic.name</div>
);

const KafkaDetails = ({consumerGroup, topics}: Props) => (
  <Card
    actions={[
      <a href="#">Add a topic</a>,
    ]}>
    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
      <Label style={{ lineHeight: '18px' }}>
        <Icon type="sound" style={{ paddingRight: 5, fontSize: 18 }} />Kafka
      </Label>
      <Label style={{ lineHeight: '18px' }}>
        <small>{consumerGroup}</small>
      </Label>
    </div>
    <List
      dataSource={topics}
      renderItem={renderTopic}
      locale={{ emptyText: 'No topics yet' }} />
  </Card>
);

export default KafkaDetails;
