import React, { Component } from 'react';
import {
  Row,
  Col,
  Form,
  Input,
  Select,
  Button,
  List,
  Tabs,
} from 'antd';
import { connect } from 'react-redux';
import SyntaxHighlighter from 'react-syntax-highlighter';
import { solarizedDark } from 'react-syntax-highlighter/styles/hljs';

import ValueDisplay from '../ValueDisplay';
import { applicationFormChanged, createApplication } from './actions';

const ApplicationForm = Form.create({
  onFieldsChange(props, changedFields) {
    props.onChange(changedFields);
  },
  mapPropsToFields(props) {
    return {
      name: Form.createFormField({ value: props.applicationForm.name }),
    };
  }
})(({
  form: {
    getFieldDecorator
  },
  workspace,
  createApplication,
  creating,
}) => {
  const prefixSelector = getFieldDecorator('database', {})(
    <Select>
      { workspace.data.map(db => <Select.Option key={db.id} value={db.name}>{db.name}</Select.Option>) }
    </Select>
  );
  return (
    <Form onSubmit={e => {
      createApplication();
      e.preventDefault();
    }}>
      <Form.Item>
        {getFieldDecorator('name', {
            rules: [{ required: true, message: 'please provide an application name' }],
          })(<Input placeholder="application name" size="large" />)}
      </Form.Item>
      <Form.Item>
        <Button loading={creating} htmlType="submit" size="large" type="primary" style={{ width: '100%' }}>Create</Button>
      </Form.Item>
    </Form>
  );
});

const ApplicationItem = ({ application }) => {
  const consumer = `$ bin/kafka-console-consumer.sh --zookeeper localhost:2181 --application ${application.name} --consumer-property group.id=${application.consumer_group}`;
  return (
    <div>
      <Row type="flex" justify="center">
        <Col span={12}>
          <ValueDisplay label="name">
            {application.name}
          </ValueDisplay>
        </Col>
        <Col span={12}>
          <ValueDisplay label="consumer group">
            {application.consumer_group}
          </ValueDisplay>
        </Col>
      </Row>
      <h2>Create a consumer for your application in a shell:</h2>
      <SyntaxHighlighter language="shell" style={solarizedDark}>
        {consumer}
      </SyntaxHighlighter>
    </div>
  );
}

class Applications extends Component {

  render() {
    const { applications, activeWorkspace, applicationForm, applicationFormChanged, createApplication, creating, } = this.props;
    applicationFormChanged({ database: { value: activeWorkspace.data[0].name } });
    return (
      <Row>
        <Col span={16}>
          {activeWorkspace.applications.length == 0 && (
            <h2>No applications yet. Create one using the form to the right.</h2>
          )}
          {activeWorkspace.applications.length == 1 && <ApplicationItem application={activeWorkspace.applications[0]} />}
          {activeWorkspace.applications.length > 1 && (
            <Tabs>
              {activeWorkspace.applications.map(application => (
                <Tabs.TabPane tab={application.name} key={application.name}>
                  <ApplicationItem application={application} />
                </Tabs.TabPane>
              ))}
            </Tabs>
          )}
        </Col>
        <Col offset={1} span={6}>
          <h3>Create a new application:</h3>
          <ApplicationForm
            creating={creating}
            createApplication={createApplication}
            onChange={applicationFormChanged}
            workspace={activeWorkspace}
            applicationForm={applicationForm} />
        </Col>
      </Row>
    );
  }
}

export default connect(
  state => ({
    ...state.workspaces.applications,
    activeWorkspace: state.workspaces.details.activeWorkspace
  }), {
    applicationFormChanged,
    createApplication,
  }
)(Applications);