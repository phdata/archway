import * as React from 'react';
import { Icon } from 'antd';

interface Props {
  behavior: string;
  name: string;
  summary: string;
}

const Info = ({ behavior, name, summary }: Props) => (
  <div style={{ textAlign: 'center' }}>
    <Icon style={{ fontSize: 72 }} type={behavior === 'simple' ? 'team' : 'deployment-unit'} />
    <div style={{ fontSize: 32 }}>{name}</div>
    <div style={{ fontSize: 16, marginTop: -4 }}>{summary}</div>
  </div>
);

export default Info;
