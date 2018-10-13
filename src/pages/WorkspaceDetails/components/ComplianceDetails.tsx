import * as React from 'react';
import { Icon, Card } from 'antd';
import { Label } from '.';

interface ItemProps {
    checked: boolean;
    children: string;
    icon: string;
}

const ComplianceItem = ({ checked, children, icon }: ItemProps) => (
  <div style={{ display: 'flex', flex: 1, flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
    <Icon
      type={checked ? icon : 'dash'}
      theme={checked ? 'twoTone' : 'outlined'}
      twoToneColor={checked ? 'red' : '#000'}
      style={{ marginBottom: 5, fontSize: 28 }} />
    <div style={{ letterSpacing: 1 }}>{children}</div>
  </div>
);

interface Props {
    pci: boolean;
    phi: boolean;
    pii: boolean;
}

const ComplianceDetails = ({ pci, phi, pii }: Props) => (
  <Card
    style={{ display: 'flex', flex: 1 }}
    bodyStyle={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
    <Label>compliance</Label>
    <div style={{ display: 'flex', flex: 1, alignItems: 'center' }}>
      <ComplianceItem checked={pci} icon="bank">PCI</ComplianceItem>
      <ComplianceItem checked={phi} icon="medicine-box">PHI</ComplianceItem>
      <ComplianceItem checked={pii} icon="idcard">PII</ComplianceItem>
    </div>
  </Card>
);

export default ComplianceDetails;
