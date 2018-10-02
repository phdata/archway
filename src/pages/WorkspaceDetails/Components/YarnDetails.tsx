import { Card, Icon, List } from 'antd';
import * as React from 'react';
import { YarnApplication, PoolInfo } from '../../../types/Workspace';
import Label from './Label';
import Colors from '../../../components/Colors';

interface Props {
    poolName: string;
    pools?: PoolInfo[];
}

const renderApplication = (yarnApplication: YarnApplication) => (
  <div style={{ margin: 10, textAlign: 'center' }}>
    {yarnApplication.name}
  </div>
);

const YarnDetails = ({ poolName, pools }: Props) => (
  <Card
    actions={[
      <a href="#">Open Resource Manger</a>,
    ]}>
    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
      <Label style={{ lineHeight: '22px' }}>
        <Icon
          theme="twoTone"
          twoToneColor={Colors.Green.string()}
          type="rocket"
          style={{ paddingRight: 5, fontSize: 22 }} />Yarn
      </Label>
      <Label style={{ lineHeight: '18px', fontSize: 10 }}>
        {poolName}
      </Label>
    </div>
    <List
      grid={{ column: 3, gutter: 12 }}
      dataSource={pools && pools[0].applications}
      renderItem={renderApplication}
      locale={{ emptyText: 'No running applications' }} />
  </Card>
);

export default YarnDetails;
