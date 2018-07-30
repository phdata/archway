import React from 'react';
import { Checkbox, Spin, Radio, Icon, Form, Input, Row, Col, Button } from 'antd';
import { connect } from 'react-redux';

import { workspaceRequested, setRequestType, requestChanged } from './actions';
import './Request.css';

const FormItemDefaults = {
  labelCol: {
    span: 10,
  },
  wrapperCol: {
    span: 12,
  },
};

const complianceOptions = [
  { label: 'PCI', value: 'pci' },
  { label: 'PHI', value: 'phi' },
  { label: 'PII', value: 'pii' },
];

const RequestForm = Form.create({
  onFieldsChange(props, changedFields) {
    props.onChange(changedFields);
  },
  mapPropsToFields(props) {
    return {
      name: Form.createFormField({
        value: props.pendingRequest.name,
      }),
      disk: Form.createFormField({
        value: props.pendingRequest.disk,
      }),
      cores: Form.createFormField({
        value: props.pendingRequest.cores,
      }),
      memory: Form.createFormField({
        value: props.pendingRequest.memory,
      }),
      compliance: Form.createFormField({
        value: props.pendingRequest.compliance ? Object.entries(props.pendingRequest.compliance).filter(i => i[1]).map(i => i[0].substring(0, 3)) : null,
      }),
    };
  },
})(({
  form: {
    getFieldDecorator,
  },
  defaultType,
  typeChanged,
  pendingRequestType,
}) => (
  <Form layout="horizontal" style={{ margin: 10, padding: 15, background: '#F0F3F5' }}>
    <Form.Item label="Workspace Type" {...FormItemDefaults}>
      <Radio.Group size="large" style={{ width: '100%' }} defaultValue={defaultType} onChange={p => typeChanged(p.target.value)}>
        <Radio.Button value="user" style={{ width: '33%', textAlign: 'center' }}>
          <Icon type="user" />&nbsp;Personal
        </Radio.Button>
        <Radio.Button value="simple" style={{ width: '33%', textAlign: 'center' }}>
          <Icon type="team" />&nbsp;Simple
        </Radio.Button>
        <Radio.Button value="structured" style={{ width: '33%', textAlign: 'center' }}>
          <Icon type="copy" />&nbsp;Structured
        </Radio.Button>
      </Radio.Group>
    </Form.Item>
    {pendingRequestType && pendingRequestType !== 'user' &&
    <div>
      <Form.Item label="Name" {...FormItemDefaults} wrapperCol={{ span: 12 }}>
        {getFieldDecorator('name', {})(<Input />)}
      </Form.Item>
      <Form.Item label="Data May Contain" {...FormItemDefaults}>
        {getFieldDecorator('compliance', {})(<Checkbox.Group options={complianceOptions} />)}
      </Form.Item>
      <Form.Item label="Disk Quota (HDFS)" {...FormItemDefaults} wrapperCol={{ span: 8 }}>
        {getFieldDecorator('disk', {})(<Input type="number" addonAfter="GB" />)}
      </Form.Item>
      <Form.Item label="Max Cores (Resource Pool)" {...FormItemDefaults} wrapperCol={{ span: 8 }}>
        {getFieldDecorator('cores', {})(<Input type="number" />)}
      </Form.Item>
      <Form.Item label="Max Memory (Resource Pool)" {...FormItemDefaults} wrapperCol={{ span: 8 }}>
        {getFieldDecorator('memory', {})(<Input type="number" addonAfter="GB" />)}
      </Form.Item>
    </div>
      }
  </Form>
));

const flex = {
  display: 'flex',
  flexDirection: 'column',
  flex: 1,
}

const GeneratedWorkspace = ({ workspace }) => (
  <div style={{ ...flex, alignItems: 'center' }}>
    <div>
      <span className="Highlight">{workspace.data.length}</span> Hive database(s) will be requested
      <ul>
        {workspace.data.map(item => <li key={item.name}>{item.name}</li>)}
      </ul>
    </div>
    <div>
      <span className="Highlight">{workspace.processing.length}</span> Resource pool(s) will be requested
      <ul>
        {workspace.processing.map(item => <li key={item.name}>{item.pool_name}</li>)}
      </ul>
    </div>
  </div>
);

const Request = ({
  setRequestType,
  requestForm,
  pendingRequest,
  pendingWorkspace,
  requestChanged,
  pendingRequestType,
  generating,
  workspaceRequested,
  requesting,
}) => (
  <div>
    <Row type="flex">
      <Col span={12}>
        <RequestForm
          pendingRequest={pendingRequest}
          pendingRequestType={pendingRequestType}
          defaultType={pendingRequestType}
          onChange={requestChanged}
          typeChanged={setRequestType}
        />
      </Col>
      <Col span={12} style={flex}>
        {!generating && !pendingWorkspace && <h3 style={{ ...flex, justifyContent: 'center' }}><Icon type="arrow-left">Start by selecting a type of workspace on the left</Icon></h3>}
        {generating && <Spin spinning={generating} tip="generating" style={{ width: '100%' }} /> }
        {pendingWorkspace && !generating && <GeneratedWorkspace workspace={pendingWorkspace} /> }
      </Col>
    </Row>
    <Row style={{ marginTop: 25 }}>
      <Col align="center">
        <Button disabled={!pendingWorkspace} loading={requesting} size="large" type="primary" onClick={workspaceRequested}>Submit for Approval</Button>
      </Col>
    </Row>
  </div>
);

export default connect(
  state => state.workspaces,
  {
    setRequestType,
    requestChanged,
    workspaceRequested,
  },
)(Request);
