import * as React from 'react';
import { Card, Button } from 'antd';
import { ProvisioningType } from '../../../../constants';

interface Props {
  children?: any;
  provisioning: ProvisioningType;
  onDeprovisionWorkspace: (e: React.MouseEvent) => void;
}

const DeprovisionCard = ({ children, provisioning, onDeprovisionWorkspace }: Props) => {
  return (
    <Card style={{ height: '100%' }} bordered>
      <div style={{ textAlign: 'center', fontSize: 17 }}>{children}</div>
      <div
        style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          fontWeight: 300,
          padding: '32px 0 48px 0',
        }}
      >
        <Button type="primary" onClick={onDeprovisionWorkspace} disabled={provisioning === ProvisioningType.Pending}>
          Deprovision
        </Button>
      </div>
    </Card>
  );
};

export default DeprovisionCard;
