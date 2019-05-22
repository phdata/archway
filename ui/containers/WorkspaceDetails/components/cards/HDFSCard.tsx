import * as React from 'react';
import { Card, Icon } from 'antd';
import { HiveAllocation } from '../../../../models/Workspace';

interface Props {
  data: HiveAllocation[];
}

const HDFSCard = ({ data }: Props) => (
  <Card bodyStyle={{ paddingTop: 48, textAlign: 'center' }} bordered>
    <Icon type="folder" style={{ fontSize: 48 }} />
    <div style={{ fontSize: 18, lineHeight: 2 }}>HDFS</div>
    <div
      style={{
        height: 100,
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
      }}
    >
      {(data && data.length > 0) ? data.map((allocation: HiveAllocation, index: number) => (
        <div key={index} style={{ fontSize: 12 }}>{allocation.location}</div>
      )) : (
        <div style={{ fontSize: 12 }}>No locations.</div>
      )}
    </div>
  </Card>
);

export default HDFSCard;
