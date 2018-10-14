import * as React from 'react';
import { Icon, Card } from 'antd';
import Label from './Label';
import { ApprovalItem } from '../../../types/Workspace';

interface ItemProps {
  approvalDate?: string;
  children: any;

  approve: ((e: React.MouseEvent) => void) | false;
}

const Approval = ({ approvalDate, children, approve }: ItemProps) => (
  <div style={{ display: 'flex', flex: 1, flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
    <Icon
      type={approvalDate ? 'safety-certificate' : 'dash'}
      theme={approvalDate ? 'twoTone' : 'outlined'}
      style={{ marginBottom: 5, fontSize: 28 }} />
    <div style={{ textTransform: 'uppercase', letterSpacing: 1 }}>{children}</div>
    {!approvalDate && approve && (<a onClick={approve}>APPROVE</a>)}
  </div>
);

interface Props {
  infra?: ApprovalItem;
  risk?: ApprovalItem;

  approveRisk: ((e: React.MouseEvent) => void) | false;
  approveOperations: ((e: React.MouseEvent) => void) | false;
}

const ApprovalDetails = ({ infra, risk, approveOperations, approveRisk }: Props) => (
  <Card
    style={{ display: 'flex', flex: 1 }}
    bodyStyle={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
    <Label>approvals</Label>
    <div style={{ display: 'flex', flex: 1, alignItems: 'center' }}>
      <Approval
        approvalDate={risk && risk.approval_time}
        approve={approveRisk}>
        risk
      </Approval>
      <Approval
        approvalDate={infra && infra.approval_time}
        approve={approveOperations}>
        ops
      </Approval>
    </div>
  </Card>
);

export default ApprovalDetails;
