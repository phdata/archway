import * as React from 'react';
import {Card, Avatar} from 'antd';
import {Statusable, Status, ServiceLinks} from '../types/Cluster';

export interface Props {
  name: String
  status: Status<Statusable>
  links: ServiceLinks<Statusable>
  index: number
}

const ServiceDisplay = ({name, status, links, index}: Props) => (
  <Card
    style={{ flex: 1, marginLeft: index == 0 ? 0 : 25 }}
    actions={links.links}>
    <Card.Meta
      title={name}
      description={`${name}'s status is currently ${status.statusText()}`}
      avatar={
        <Avatar
          size="small"
          style={{ boxShadow: status.glowColorText(), backgroundColor: status.statusColor().string() }} />
      } />
  </Card>
);

export default ServiceDisplay;