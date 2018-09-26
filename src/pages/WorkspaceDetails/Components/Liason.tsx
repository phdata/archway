import * as React from 'react';
import { Card, Icon } from 'antd';
import Label from './Label';

interface Props {
  liason: string;
}

const Liason = ({liason}: Props) => (
  <Card
    style={{ display: 'flex', flex: 1 }}
    bodyStyle={{ display: 'flex' flexDirection: 'column', flex: 1 }}>
    <Label>liason</Label>
    <div
      style={{
          textAlign: 'center',
          display: 'flex',
          flexDirection: 'column',
          flex: 1,
          alignItems: 'center',
          justifyContent: 'center',
        }}>
      <Icon type="crown" theme="twoTone" twoToneColor="#D7C9AA" style={{ marginBottom: 5, fontSize: 28 }} />
      <div style={{ letterSpacing: 1, textTransform: 'uppercase' }}>{liason}</div>
    </div>
  </Card>
);

export default Liason;
