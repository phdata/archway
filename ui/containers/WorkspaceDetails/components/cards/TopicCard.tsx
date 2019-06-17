import * as React from 'react';
import { Card, Row } from 'antd';

import CardHeader from './CardHeader';
import { KafkaTopic } from '../../../../models/Workspace';

interface Props {
  topic: KafkaTopic;
}

function getTopicHeading(topic: KafkaTopic) {
  const { name } = topic;
  const parts = name.split('.');
  if (parts.length > 0) {
    return parts[parts.length - 1];
  }

  return '';
}

const TopicCard = ({ topic }: Props) => (
  <Card>
    <CardHeader>
      <div style={{ flex: 1, textAlign: 'center' }}>
        {getTopicHeading(topic)}
        <div style={{ fontSize: 12, textTransform: 'none' }}>{topic.name}</div>
      </div>
    </CardHeader>
    <div style={{ display: 'flex', textAlign: 'center' }}>
      <Row gutter={12} style={{ fontSize: 16, fontWeight: 300, flex: 1 }}>
        PARTITIONS
        <br />
        <span style={{ fontSize: 14 }}>{topic.partitions}</span>
      </Row>
      <Row gutter={12} style={{ fontSize: 16, fontWeight: 300, flex: 1 }}>
        REPLICATION FACTOR
        <br />
        <span style={{ fontSize: 14 }}>{topic.replication_factor}</span>
      </Row>
    </div>
  </Card>
);

export default TopicCard;
