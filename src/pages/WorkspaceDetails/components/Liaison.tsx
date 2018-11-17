import * as React from 'react';
import { Card, Icon } from 'antd';
import Label from './Label';
import { Member } from '../../../types/Workspace';

interface Props {
  liaison?: Member;
}

const Liason = ({ liaison }: Props) => (
  <Card
    style={{ display: 'flex', flex: 1 }}
    bodyStyle={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
    <Label>liaison</Label>
    <div
      style={{
        textAlign: 'center',
        display: 'flex',
        flexDirection: 'column',
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
      }}>
      <Icon type="crown" theme="twoTone" twoToneColor="#D7C9AA" style={{ marginBottom: 5, fontSize: 42 }} />
      <div style={{ letterSpacing: 1, textTransform: 'uppercase' }}>{liaison && liaison.name}</div>
    </div>
  </Card>
);

export default Liason;
