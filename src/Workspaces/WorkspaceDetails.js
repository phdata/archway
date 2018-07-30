import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Spin, Row, Col, Icon, Button, Tabs, Tag, Menu, List, Input, Form, Avatar } from 'antd';

import TabIcon from './TabIcon';
import DBDisplay from './DBDisplay';
import ProcessingDisplay from './ProcessingDisplay';
import ValueDisplay from './ValueDisplay';
import { changeDB, approveInfra, approveRisk, getWorkspace, existingMemberSelected, newMemberSelected } from './actions';
import './WorkspaceDetails.css';

const UsernameForm = Form.create({
  onFieldsChange(props, changedFields) {
    props.onChange(changedFields);
  },
  mapPropsToFields(props) {
    return {
      filter: Form.createFormField({
        value: props.memberForm.filter,
      }),
    };
  },
})(({
  form: {
    getFieldDecorator,
  },
}) => (
  <Form onSubmit={(e) => {e.preventDefault()}}>
      <Form.Item>
        {getFieldDecorator('filter', {})(
          <Input.Search
            placeholder="add or find a member..."
            size="large"
          />
        )}
      </Form.Item>
  </Form>
));

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

const Applications = () => (<div />);

const Member = ({ member, selected }) => (
  <List.Item style={{ cursor: 'pointer', backgroundColor: selected ? '#B3B9C0' : 'white' }} actions={[<Icon type="right" />]}>
    <div style={{ display: 'flex', alignItems: 'center' }}>
      <Avatar icon="user" />
      <h3 style={{ marginLeft: 10, marginBottom: 0 }}>{member.username}</h3>
    </div>
  </List.Item>
);

const Members = ({
  filterChanged,
  memberForm,
  existingMembers,
  existingSelected,
  newMembers,
  newSelected,
}) => (
  <Row>
    <Col span={8}>
      <div>
        <UsernameForm onChange={filterChanged} memberForm={memberForm} />
        {existingMembers && (
          <List
            bordered
            dataSource={existingMembers}
            renderItem={item => <Member onSelect={newSelected} member={item} />}
          />
        )}
        {newMembers && (
          <List
            header={<h3>or add a new member...</h3>}
            dataSource={newMembers}
            renderItem={item => <Member onSelect={existingSelected} member={item} />}
          />
        )}
      </div>
    </Col>
  </Row>
);

const Status = ({ workspace: { status, approvals } }) => (
  <div>
    <h2>Overall Status: {status}</h2>
    <Row type="flex" justify="center">
      <Col span={8}>
        <ValueDisplay label="Infrastructure" color={(approvals && approvals.infra) ? "#0B7A75" : "#FF5900"}>
          {(approvals && approvals.infra) ? "approved" : "pending"}
        </ValueDisplay>
      </Col>
      <Col span={8}>
        <ValueDisplay label="Risk" color={(approvals && approvals.infra) ? "#0B7A75" : "#FF5900"}>
          {(approvals && approvals.Risk) ? "approved" : "pending"}
        </ValueDisplay>
      </Col>
    </Row>
  </div>
);

class WorkspaceDetails extends React.Component {

  componentDidMount() {
    this.props.getWorkspace(this.props.match.params.id);
  }

  render() {
    const { workspaces: { activeWorkspace, memberForm, existingMembers, newMembers }, cluster } = this.props;
    if (!activeWorkspace)
      return <Spin />;
    return (
      <div className="WorkspaceDetails">
        <h1>{activeWorkspace && activeWorkspace.name}</h1>
        <Tabs activeKey={"members"} size="large">
          <Tabs.TabPane key="status" tab={<span><Icon type="info-circle-o" /> Status</span>}>
            <Status workspace={activeWorkspace} />
          </Tabs.TabPane>
          <Tabs.TabPane key="data" tab={<span><Icon type="database" /> Data</span>}>
            <DBDisplay workspace={activeWorkspace} cluster={cluster} />
          </Tabs.TabPane>
          <Tabs.TabPane key="processing" tab={<span><Icon type="dashboard" /> Processing</span>}>
            <ProcessingDisplay />
          </Tabs.TabPane>
          <Tabs.TabPane key="applications" tab={<span><Icon type="code" /> Applications</span>}>
            <Applications />
          </Tabs.TabPane>
          <Tabs.TabPane key="members" tab={<span><Icon type="team" /> Members</span>}>
            <Members memberForm={memberForm} existingMembers={existingMembers} newMembers={newMembers} />
          </Tabs.TabPane>
        </Tabs>
      </div>
    );
  }

}

WorkspaceDetails.propTypes = {
  getWorkspace: PropTypes.func.isRequired,
};

export default connect(
  state => ({
    workspaces: state.workspaces,
    cluster: state.cluster,
  }), {
    getWorkspace,
    existingMemberSelected,
    newMemberSelected,
  }
)(WorkspaceDetails);