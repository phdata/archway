import * as React from 'react';
import { Card, Icon } from 'antd';

import { HiveAllocation } from '../../../../models/Workspace';

interface Props {
  data: HiveAllocation[];
}

const HiveCard = ({ data }: Props) => (
  <Card bodyStyle={{ paddingTop: 48, textAlign: 'center' }} bordered>
    <Icon type="database" style={{ fontSize: 48 }} />
    <div style={{ fontSize: 18, lineHeight: 2 }}>HIVE</div>
    <div
      style={{
        height: 100,
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
      }}
    >
      {data && data.length > 0 ? (
        data.map((allocation: HiveAllocation, index: number) => (
          <div key={index} style={{ fontSize: 12 }}>
            {allocation.name}
          </div>
        ))
      ) : (
        <div style={{ fontSize: 12 }}>No database.</div>
      )}
    </div>
  </Card>
);

export default HiveCard;
