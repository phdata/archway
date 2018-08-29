import React from 'react';
import { connect } from 'react-redux';
import {
  Row,
  Col,
  Icon,
  Button,
} from 'antd';

import Panel from '../Panel';
import { approveRisk, approveInfra } from './actions';

const ComplianceCheck = ({ label, present }) => (
  <div style={{ display: 'flex', flexDirection: 'column', justifyItems: 'space-between' }}>
    <Icon style={{ fontSize: 28, color: present ? "#FF5900" : "#0B7A75" }} type={present ? "warning" : "minus-circle-o"} />
    <h3>{label}</h3>
  </div>
);

const ApprovalActions = ({ approving, approved, canApprove, approve, label }) => (
  <Panel style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
    <div style={{
      fontWeight: 100,
      fontSize: 24,
      color: approved ? "#0B7A75" : "#FF5900",
      overflow: 'hidden'
    }}>
      {approved ? "approved" : "pending"}
    </div>
    <div>
      {label}
    </div>
    {canApprove && (
      <Button
        icon="check"
        disabled={approved}
        loading={approving}
        type="primary"
        size="large"
        onClick={approve}>
        Approve {label}
      </Button>
    )}
  </Panel>
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
  <div>
    <h2>Approvals</h2>
    <hr />
    <Row type="flex" justify="center">
      <Col span={8}>
        <ApprovalActions
          label="Infrastructure"
          approving={approving}
          approved={approvals && approvals.infra}
          canApprove={platform_operations}
          approve={approveInfra} />
      </Col>
      <Col span={8}>
        <ApprovalActions
          label="Risk"
          approving={approving}
          approved={approvals && approvals.risk}
          canApprove={risk_management}
          approve={approveRisk} />
      </Col>
    </Row>
    <h2>Compliance</h2>
    <hr />
    <Row type="flex" justify="center">
      <Col span={4}>
        <ComplianceCheck
          label="PII"
          present={pii_data} />
      </Col>
      <Col span={4}>
        <ComplianceCheck
          label="PCI"
          present={pci_data} />
      </Col>
      <Col span={4}>
        <ComplianceCheck
          label="PHI"
          present={phi_data} />
      </Col>
    </Row>
  </div>
);

export default connect(
  state => state.workspaces.status, { approveRisk, approveInfra }
)(Status);
