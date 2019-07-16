import * as React from 'react';
import { Icon } from 'antd';

import { ProvisioningType } from '../../../constants';

interface Props {
  provisioning: ProvisioningType;
}

const Provisioning = ({ provisioning }: Props) => (
  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', paddingTop: 8 }}>
    <Icon
      type={provisioning === ProvisioningType.Pending ? 'clock-circle' : 'check-circle'}
      theme="twoTone"
      style={{ fontSize: 20, paddingRight: 8 }}
    />
    <span style={{ fontSize: 14 }}>{`PROVISIONING: ${provisioning.toUpperCase()}`}</span>
  </div>
);

export default Provisioning;
