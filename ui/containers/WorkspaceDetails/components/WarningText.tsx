import * as React from 'react';
import { Icon } from 'antd';

interface Props {
  message: string;
}

const WarningText = ({ message }: Props) => (
  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
    <Icon type="warning" theme="filled" style={{ fontSize: 32, color: 'red' }} />
    <p style={{ paddingLeft: 10, margin: 0 }}>{message}</p>
  </div>
);

export default WarningText;
