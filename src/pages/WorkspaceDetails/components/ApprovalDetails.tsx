import * as React from 'react';
import { Icon, Card } from 'antd';
import Label from './Label';
import { ApprovalItem } from '../../../types/Workspace';

interface ItemProps {
    approvalDate?: string;
    icon: string;
    children: any;
}

const Approval = ({ approvalDate, icon, children }: ItemProps) => (
  <div style={{ display: 'flex', flex: 1, flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
    <Icon
      type={approvalDate ? icon : 'dash'}
      theme={approvalDate ? 'twoTone' : 'outlined'}
      style={{ marginBottom: 5, fontSize: 28 }} />
    <div style={{ textTransform: 'uppercase', letterSpacing: 1 }}>{children}</div>
  </div>
);

interface Props {
    infra?: ApprovalItem;
    risk?: ApprovalItem;
}

const ApprovalDetails = ({ infra, risk }: Props) => (
  <Card
    style={{ display: 'flex', flex: 1 }}
    bodyStyle={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
    <Label>approvals</Label>
    <div style={{ display: 'flex', flex: 1, alignItems: 'center' }}>
      <Approval approvalDate={risk && risk.approval_time} icon="safety-certificate">risk</Approval>
      <Approval approvalDate={infra && infra.approval_time} icon="dashboard">ops</Approval>
    </div>
  </Card>
);

export default ApprovalDetails;
