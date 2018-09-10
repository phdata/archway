import React, { Component } from 'react';
import { Button, Form, Input, Col, Row } from 'antd';
import { withFormik } from 'formik';
import { connect } from 'react-redux';

import { setRequest, workspaceRequested, setRequestType, requestChanged } from './actions';

const StepItem = ({ children, selected }) => (
  <div style={{ fontSize: 12, letterSpacing: 1, textTransform: 'uppercase', fontWeight: selected ? 'bold' : 'normal', padding: 5, borderBottom: selected ? '1px solid black' : 'none' }}>
    {children}
  </div>
);

const Steps = ({ current, style = {} }) => (
  <div style={{ display: 'flex', margin: 'auto', justifyContent: 'space-evenly', flexDirection: 'row', ...style }}>
    <StepItem selected={current === 'overview'}>Overview</StepItem>
    <StepItem selected={current === 'structure'}>Structure</StepItem>
    <StepItem selected={current === 'review'}>Review</StepItem>
  </div>
);

const FieldLabel = ({ children }) => (
  <div style={{ color: 'rgba(0, 0, 0, .5)', textAlign: 'center', textTransform: 'uppercase', fontSize: 10 }}>
    {children}
  </div>
)

const OverviewPage = ({ handleChange, handleSubmit, values: { name, summary, description, phi_data, pii_data, pci_data } }) => (
  <Form
    layout="vertical"
    onSubmit={handleSubmit}>
    <Form.Item
      label={<FieldLabel>Workspace Name</FieldLabel>}>
      <Input
        name="name"
        placeholder="Loan Modification Group (LMG)"
        value={name}
        onChange={handleChange} />
    </Form.Item>
    <Form.Item
      label={<FieldLabel>Summary</FieldLabel>}>
      <Input
        name="summary"
        placeholder="LMG's place to evaluate modification algorithms"
        value={summary}
        onChange={handleChange} />
    </Form.Item>
    <Form.Item
      label={<FieldLabel>Purpose for workspace</FieldLabel>}>
      <Input.TextArea
        name="description"
        placeholder="(Use this area to fully describe to reviewers and future users why this workspace is needed.)"
        rows={5}
        value={description}
        onChange={handleChange} />
    </Form.Item>
    <Form.Item style={{ textAlign: 'center' }}>
      <Button style={{ width: '50%', textTransform: 'uppercase' }} size="large" type="primary" htmlType="submit">
        Next
      </Button>
    </Form.Item>
  </Form>
);

const OverviewPageForm = withFormik({
  handleSubmit: (payload, { props }) => {
    props.setRequest(payload);
  }
})(OverviewPage);

const WorkspaceRequest = ({ currentStep = 'overview' }) => (
  <div>
    <div style={{ textAlign: 'center' }}>
      <h1>New Workspace Request</h1>
      <Steps style={{ width: 350 }} current={currentStep} />
    </div>
    <Row style={{ margin: 25 }} type="flex" justify="center">
      <Col span={12}>
        <OverviewPageForm />
      </Col>
    </Row>
  </div>
);

export default connect(
  state => state,
  { setRequest }
)(WorkspaceRequest);
