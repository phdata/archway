import * as React from 'react';
import { Card, Icon } from 'antd';

import { ResourcePool } from '../../../../models/Workspace';

interface Props {
  data: ResourcePool[];
}

const PoolCard = ({ data }: Props) => (
  <Card bodyStyle={{ paddingTop: 48, textAlign: 'center' }} bordered>
    <Icon type="api" style={{ fontSize: 48 }} />
    <div style={{ fontSize: 18, lineHeight: 2 }}>RESOURCE POOL</div>
    <div
      style={{
        height: 100,
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
      }}
    >
      {(data && data.length > 0) ? data.map((pool: ResourcePool, index: number) => (
        <div key={index} style={{ fontSize: 12 }}>{pool.pool_name}</div>
      )) : (
        <div style={{ fontSize: 12 }}>No pools.</div>
      )}
    </div>
  </Card>
);

export default PoolCard;
