import * as React from 'react';
import { Card, Icon } from 'antd';

import CardHeader from './CardHeader';
import { Application } from '../../../../models/Workspace';

interface Props {
  application: Application
}

const ApplicationsInfoCard = ({ application }: Props) => (
  <Card bordered style={{ marginBottom: 16 }}>
    <CardHeader>
      <Icon style={{ fontSize: 36, marginRight: 12 }} type="rocket" />
      {application.name}
    </CardHeader>
    <div style={{ display: 'flex', alignItems: 'center', marginBottom: 16 }}>
      <div style={{ flex: 1 }}>
        <div style={{ fontSize: 16 }}>PRINCIPAL</div>
        <div style={{ fontWeight: 300 }}>app_loan_modification_group_default</div>
        {/* <a style={{ fontWeight: 300 }} href="#">regenerate and download keytab</a> */}
      </div>
      <Icon style={{ fontSize: 24 }} type="right" />
    </div>
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
  </Card>
);

export default ApplicationsInfoCard;
