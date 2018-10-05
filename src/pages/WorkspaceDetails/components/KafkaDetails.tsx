import { Card, Row } from 'antd';
import * as React from 'react';
import { KafkaTopic } from '../../../types/Workspace';
import CardHeader from './CardHeader';

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
    <CardHeader
      icon="sound"
      heading="Kafka Topics"
      subheading={consumerGroup} />
    <Row gutter={12} type="flex" justify="center" style={{ marginTop: 18 }}>
      {topics && topics.length > 0 && topics.map(renderTopic)}
      {(!topics || topics.length <= 0) && (
        <div style={{ color: 'rgba(0, 0, 0, .65)' }}>
          No topics yet.
        </div>
      )}
    </Row>
  </Card>
);

export default KafkaDetails;
