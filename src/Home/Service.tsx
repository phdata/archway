import * as React from 'react';
import {Card, Avatar} from 'antd';

export interface ServiceInfo {
  name: string
  status: string
  statusColor: string
  statusGlowColor: string
  service: object
  index: number
}

interface LocalProps {
  links: JSX.Element[]
}

const Service = ({ name, status, statusColor, statusGlowColor, links, index }: ServiceInfo & LocalProps) => (
  <Card
    style={{ flex: 1, marginLeft: index == 0 ? 0 : 25 }}
    actions={links}>
    <Card.Meta
      title={name}
      description={`${name}'s status is currently ${status}`}
      avatar={
        <Avatar
          size="small"
          alt={`${name}'s status is currently ${status}`}
          style={{ boxShadow: statusGlowColor, backgroundColor: statusColor }}
          />
      } />
  </Card>
);

export default Service;