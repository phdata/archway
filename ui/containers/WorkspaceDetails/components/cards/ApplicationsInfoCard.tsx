import * as React from 'react';
import { Card, Icon } from 'antd';

import CardHeader from './CardHeader';
import { Application } from '../../../../models/Workspace';

interface Props {
  application: Application;
}

const ApplicationsInfoCard = ({ application }: Props) => (
  <Card bordered>
    <div style={{ display: 'flex', alignItems: 'center' }}>
      <div style={{ flex: 1 }}>
        <CardHeader>
          <Icon style={{ fontSize: 36, marginRight: 12 }} type="rocket" />
          <div>
            {application.name}
            <div style={{ fontSize: 12, textTransform: 'none' }}>{application.consumer_group}</div>
          </div>
        </CardHeader>
        <div style={{ display: 'flex' }}>
          <div style={{ fontSize: 16, fontWeight: 300, flex: 1 }}>
            CORES
            <br />
            <span style={{ fontSize: 14 }}>10</span>
          </div>
          <div style={{ fontSize: 16, fontWeight: 300, flex: 2 }}>
            MEMORY
            <br />
            <span style={{ fontSize: 14 }}>64Gb</span>
          </div>
        </div>
      </div>
      <Icon style={{ fontSize: 24 }} type="right" />
    </div>
  </Card>
);

export default ApplicationsInfoCard;
