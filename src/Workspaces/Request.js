import React from 'react';
import { Checkbox, Spin, Radio, Icon, Form, Input, Row, Col, Button } from 'antd';
import { connect } from 'react-redux';

import { approveInfra, approveRisk, workspaceRequested, setRequestType, requestChanged } from './actions';
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
    {pendingRequestType && pendingRequestType != 'user' &&
    <div>
      <Form.Item label="name" {...FormItemDefaults} wrapperCol={{ span: 3 }}>
        {getFieldDecorator('name', {})(<Input />)}
      </Form.Item>
      <Form.Item label="may contain" {...FormItemDefaults}>
        {getFieldDecorator('compliance', {})(<Checkbox.Group options={complianceOptions} />)}
      </Form.Item>
      <Form.Item label="disk (hdfs)" {...FormItemDefaults} wrapperCol={{ span: 3 }}>
        {getFieldDecorator('disk', {})(<Input type="number" addonAfter="GB" />)}
      </Form.Item>
      <Form.Item label="max cores (yarn)" {...FormItemDefaults} wrapperCol={{ span: 3 }}>
        {getFieldDecorator('cores', {})(<Input type="number" />)}
      </Form.Item>
      <Form.Item label="max memory (yarn)" {...FormItemDefaults} wrapperCol={{ span: 3 }}>
        {getFieldDecorator('memory', {})(<Input type="number" addonAfter="GB" />)}
      </Form.Item>
    </div>
      }
  </Form>
));

const GeneratedWorkspace = ({ data, processing }) => (
  <Row justify="center">
    <Col offset={4} span={6}>
      <span className="Highlight">{data.length}</span> Hive database(s) will be requested
      <ul>
        {data.map(item => <li key={item.name}>{item.name}</li>)}
      </ul>
    </Col>
    <Col offset={4} span={6}>
      <span className="Highlight">{processing.length}</span> Yarn queue(s) will be requested
      <ul>
        {processing.map(item => <li key={item.name}>{item.pool_name}</li>)}
      </ul>
    </Col>
  </Row>
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
    <Row>
      <Col>
        <h2>Select from the following...</h2>
        <RequestForm
          pendingRequest={pendingRequest}
          pendingRequestType={pendingRequestType}
          defaultType={pendingRequestType}
          onChange={requestChanged}
          typeChanged={setRequestType}
        />
      </Col>
    </Row>
    {pendingWorkspace &&
      <Spin spinning={generating}>
        <GeneratedWorkspace data={pendingWorkspace.data} processing={pendingWorkspace.processing} />
      </Spin>
    }
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
    approveInfra,
    approveRisk,
  },
)(Request);
