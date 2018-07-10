import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Spin, Row, Col, Icon, Button, Tabs, Tag } from 'antd';

import TabIcon from './TabIcon';
import DBDisplay from './DBDisplay';
import ProcessingDisplay from './ProcessingDisplay';
import { changeDB, approveInfra, approveRisk, getWorkspace, addMember, newMemberFormChanged } from './actions';
import './WorkspaceDetails.css';

const ComplianceCheck = ({ value, label }) => {
  if (!value) return <span />;
  return (
    <Tag color="volcano">
      <Icon type="warning" /> {label}
    </Tag>
  );
};

const Compliance = ({ compliance: { pii_data, pci_data, phi_data } }) => (
  <div style={{ marginTop: 10, marginBottom: 10 }}>
    <ComplianceCheck value={pii_data} label="PII" key="pii" />
    <ComplianceCheck value={phi_data} label="PHI" key="phi" />
    <ComplianceCheck value={pci_data} label="PCI" key="pci" />
  </div>
);

const ApprovalActions = ({
  permissions: {
    risk_management,
    platform_operations,
  },
  approvals = {},
  approving,
  approveInfra,
  approveRisk,
}) => {
  if (!risk_management && !platform_operations) return <div />;

  const infraButton = platform_operations ? <Button key="infra" disabled={!!approvals.infra} loading={approving} type="primary" onClick={approveInfra}><Icon type="check" />Infrastructure</Button> : null;
  const riskButton = risk_management ? <Button key="risk" disabled={!!approvals.risk} loading={approving} type="primary" onClick={approveRisk}><Icon type="check" />Risk</Button> : null;

  const buttons = [infraButton, riskButton].filter(i => i != null);

  return (
    <Button.Group>
      {buttons.map(i => i)}
    </Button.Group>
  );
};

class WorkspaceDetails extends React.Component {
  componentDidMount() {
    this.props.getWorkspace(this.props.match.params.id);
  }

  render() {
    const {
      activeWorkspace,
      profile,
      approveInfra,
      approveRisk,
      approving,
      changeDB,
      members,
      addMember,
      newMemberForm,
      newMemberFormChanged,
    } = this.props;
    if (!activeWorkspace) return <Spin spinning={true}>Loading...</Spin>;
    const {
      id, approvals, compliance, data, processing,
    } = activeWorkspace;
    const { permissions } = profile;
    return (
      <div className="WorkspaceDetails">
        <Row>
          <Col span={14}>
            <h1 style={{ marginBottom: 0 }}>{activeWorkspace.name}</h1>
          </Col>
          <Col span={10} align="right">
            <ApprovalActions
              approving={approving}
              approvals={approvals}
              approveInfra={approveInfra}
              approveRisk={approveRisk}
              permissions={permissions}
            />
          </Col>
        </Row>
        <Compliance compliance={compliance} />
        <div style={{ marginTop: 10, marginBottom: 10 }}>
          <Tag color={approvals && approvals.infra ? 'green' : 'red'}>
            {approvals && approvals.infra ? 'infra approved' : 'infra not approved'}
          </Tag>
          <Tag color={approvals && approvals.risk ? 'green' : 'red'}>
            {approvals && approvals.risk ? 'risk approved' : 'risk not approved'}
          </Tag>
        </div>
        <Tabs onChange={changeDB}>
          {data.map(item => (
            <Tabs.TabPane
              tab={<TabIcon icon="database" name={item.name} />}
              key={item.name}
            >
              <DBDisplay database={item} provisioned={approvals && approvals.infra && approvals.risk} />
            </Tabs.TabPane>
          ))}
          {processing.map(ProcessingDisplay)}
        </Tabs>
      </div>
    );
  }
}

WorkspaceDetails.propTypes = {
  activeWorkspace: PropTypes.object,
  profile: PropTypes.object,
  getWorkspace: PropTypes.func,
  approveInfra: PropTypes.func,
  approveRisk: PropTypes.func,
  changeDB: PropTypes.func,
};

export default connect(
  state => ({
    activeWorkspace: state.workspaces.activeWorkspace,
    profile: state.auth.profile || { permissions: { risk_management: false, platform_operations: false } },
  }),
  {
    getWorkspace,
    approveRisk,
    approveInfra,
    changeDB,
  },
)(WorkspaceDetails);
