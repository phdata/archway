import { Card, Icon, List } from 'antd';
import * as React from 'react';
import { HiveTable, NamespaceInfo } from '../../../types/Workspace';
import Label from './Label';
import { HueService } from '../../../types/Cluster';

interface Props {
  hue?: HueService;
  namespace: string;
  info?: NamespaceInfo[];
}

const renderTable = (hiveTable: HiveTable) => (
  <div>{hiveTable.name}</div>
);

const HiveDetails = ({ hue, namespace, info }: Props) => {
  return (
    <Card
      actions={[
        <a href={hue && `${hue.load_balancer[0].host}:${hue.load_balancer[0].port}/hue/metastore/tables/`}>See in Hue</a>
      ]}>
      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
        <Label style={{ lineHeight: '18px' }}>
          <Icon type="database" style={{ paddingRight: 5, fontSize: 18 }} />Hive
        </Label>
        <Label style={{ lineHeight: '18px' }}>
          <small>{namespace}</small>
        </Label>
      </div>
      <List
        dataSource={info && info[0].tables}
        renderItem={renderTable}
        locale={{ emptyText: 'No tables yet' }} />
    </Card>
  );
}

export default HiveDetails;
