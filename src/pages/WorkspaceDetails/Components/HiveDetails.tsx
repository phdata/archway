import { Card, Icon, List } from 'antd';
import * as React from 'react';
import { HiveTable, NamespaceInfo } from '../../../types/Workspace';
import Label from './Label';
import { HueService } from '../../../types/Cluster';
import Colors from '../../../components/Colors';

interface Props {
    hue?: HueService;
    namespace: string;
    info?: NamespaceInfo[];
}

const renderTable = (hiveTable: HiveTable) => (
  <div style={{ margin: 10, textAlign: 'center' }}>{hiveTable.name}</div>
);

const HiveDetails = ({ hue, namespace, info }: Props) => (
  <Card
    actions={[
      <a href={hue && `http://${hue.load_balancer[0].host}:${hue.load_balancer[0].port}/hue/metastore/tables/`}>
        See in Hue
      </a>,
    ]}>
    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
      <Label style={{ lineHeight: '20px' }}>
        <Icon
          theme="twoTone"
          twoToneColor={Colors.Green.string()}
          type="database"
          style={{ paddingRight: 5, fontSize: 20 }} />Hive
        </Label>
      <Label style={{ lineHeight: '18px', fontSize: 10 }}>
        {namespace}
      </Label>
    </div>
    <List
      dataSource={info && info[0] && info[0].tables}
      renderItem={renderTable}
      locale={{ emptyText: 'No tables yet' }} />
  </Card>
);

export default HiveDetails;
