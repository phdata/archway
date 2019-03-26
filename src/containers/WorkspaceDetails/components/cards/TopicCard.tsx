import * as React from 'react';
import { Card, Icon } from 'antd';

import CardHeader from './CardHeader';
import { KafkaTopic } from '../../../../models/Workspace';

interface Props {
  topic: KafkaTopic;
  selected?: boolean;
  onClick: () => void;
}

const cardStyle = {
  marginBottom: 16,
  cursor: 'pointer',
};

const selectedCardStyle = {
  ...cardStyle,
  borderColor: '#1DA57A',
};

function getTopicHeading(topic: KafkaTopic) {
  const { name } = topic;
  const parts = name.split('.');
  if (parts.length > 0) {
    return parts[parts.length - 1];
  }

  return '';
}

const TopicCard = ({ topic, selected, onClick }: Props) => (
  <Card bordered style={selected ? selectedCardStyle : cardStyle} onClick={onClick}>
    <div style={{ display: 'flex', alignItems: 'center' }}>
      <div style={{ flex: 1 }}>
        <CardHeader>
          <Icon style={{ fontSize: 36, marginRight: 12 }} type="notification" />
          <div>
            {getTopicHeading(topic)}
            <div style={{ fontSize: 12, textTransform: 'none' }}>
              {topic.name}
            </div>
          </div>
        </CardHeader>
        <div style={{ display: 'flex' }}>
          <div style={{ fontSize: 16, fontWeight: 300, flex: 1 }}>
            PARTITIONS
            <br />
            <span style={{ fontSize: 14 }}>{topic.partitions}</span>
          </div>
          <div style={{ fontSize: 16, fontWeight: 300, flex: 2 }}>
            REPLICATION FACTOR
            <br />
            <span style={{ fontSize: 14 }}>{topic.replication_factor}</span>
          </div>
        </div>
      </div>
      <Icon style={{ fontSize: 24 }} type="right" />
    </div>
  </Card>
);

export default TopicCard;
