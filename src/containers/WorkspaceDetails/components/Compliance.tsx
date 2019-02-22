import * as React from 'react';
import { Icon } from 'antd';

interface ItemProps {
  checked: boolean;
  children: string;
  icon: string;
}

const ComplianceItem = ({ checked, children, icon }: ItemProps) => (
  <div style={{ margin: '0 8px' }}>
    <Icon
      style={{ fontSize: 24 }}
      type={checked ? icon : 'dash'}
      theme={checked ? 'twoTone' : 'outlined'}
      twoToneColor={checked ? 'red' : '#000000'}
    />
    <div>{children}</div>
  </div>
);

interface Props {
  pci: boolean;
  phi: boolean;
  pii: boolean;
}

const Compliance = ({ pci, phi, pii }: Props) => (
  <div style={{ display: 'flex', justifyContent: 'center', paddingTop: 8 }}>
    <ComplianceItem checked={pci} icon="bank">PCI</ComplianceItem>
    <ComplianceItem checked={phi} icon="medicine-box">PHI</ComplianceItem>
    <ComplianceItem checked={pii} icon="idcard">PII</ComplianceItem>
  </div>
);

export default Compliance;
