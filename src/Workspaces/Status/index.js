import React from 'react';
import { connect } from 'react-redux';
import {
  Row,
  Col,
  Icon,
  Button,
  Card,
  Avatar,
} from 'antd';

import Panel from '../Panel';
import { approveRisk, approveInfra } from './actions';

const ComplianceCheck = ({ label, present }) => (
  <div style={{ display: 'flex', flexDirection: 'column', justifyItems: 'space-between', textAlign: 'center' }}>
    <Icon style={{ fontSize: 28, color: present ? "#FF5900" : "#0B7A75" }} type={present ? "warning" : "minus-circle-o"} />
    <div style={{ fontSize: 18 }}>
      {label}
    </div>
  </div>
);

const ApprovalActions = ({ approving, approved, canApprove, approve, label }) => (
  <Card
    style={{ flex: 1 }}
    actions={canApprove && !approved && [
      <a
        disabled={approved || approving}
        href="#"
        onClick={approve}>
        Approve
      </a>]}>
    <Card.Meta
      avatar={<Avatar size={50} style={{ backgroundColor: 'transparent', color: approved ? "#0B7A75" : "#FF5900" }} icon={approved ? "check" : "minus-circle-o"} />}
      title={label}
      description="not approved yet" />
  </Card>
);

const Status = ({
  approving,
  approveInfra,
  approveRisk,
  workspace: {
    status,
    approvals = {},
    compliance: {
      pii_data,
      phi_data,
      pci_data,
    }
  },
  profile: {
    permissions: {
      risk_management,
      platform_operations,
    }
  }
}) => (
  <div style={{ display: 'flex', marginTop: 15 }}>
    <Card
      style={{ flex: 1 }}
      title="Approvals"
      bodyStyle={{ display: 'flex', justifyContent: 'space-between' }}>
      <Card.Grid style={{ padding: 0, boxShadow: 'none' }}>
        <ApprovalActions
          label="Infrastructure"
          approving={approving}
          approved={approvals && approvals.infra}
          canApprove={platform_operations}
          approve={approveInfra} />
      </Card.Grid>
      <Card.Grid style={{ padding: 0, boxShadow: 'none' }}>
        <ApprovalActions
          label="Risk"
          approving={approving}
          approved={approvals && approvals.risk}
          canApprove={risk_management}
          approve={approveRisk} />
      </Card.Grid>
    </Card>
    <Card
      style={{ marginLeft: 15, flex: 1 }}
      title="Compliance">
      <Card.Grid>
        <ComplianceCheck
          label="PHI"
          present={phi_data} />
      </Card.Grid>
      <Card.Grid>
        <ComplianceCheck
          label="PII"
          present={pii_data} />
      </Card.Grid>
      <Card.Grid>
        <ComplianceCheck
          label="PCI"
          present={pci_data} />
      </Card.Grid>
    </Card>
  </div>
);

export default connect(
  state => state.workspaces.status, { approveRisk, approveInfra }
)(Status);
