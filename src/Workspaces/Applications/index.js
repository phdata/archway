import React, { Component } from 'react';
import {
  Row,
  Col,
  Form,
  Input,
  Button,
  Tabs,
  Card,
  Table,
  Modal
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

const CodeHelp = ({ application }) => {
  const consumer = `$ bin/kafka-console-consumer.sh --zookeeper localhost:2181 --application ${application.name} --consumer-property group.id=${application.consumer_group}`;
  return (
    <SyntaxHighlighter language="shell" style={solarizedDark}>
      {consumer}
    </SyntaxHighlighter>
  );
}

class Applications extends React.Component {

  state = {
    selectedApplication: false,
    visible: false,
    createVisible: false,
  }

  showModal = (selectedApplication) => {
    this.setState({
      selectedApplication,
      visible: true,
    });
  }

  showNewModal = () => {
    this.setState({
      createVisible: true,
    });
  }

  columns = [{
    title: 'Name',
    dataIndex: 'name',
  }, {
    title: 'Consumer Group',
    dataIndex: 'consumer_group',
  }, {
    title: 'Help',
    render: (text, record) => <a href="#" onClick={() => this.showModal(record.name)}>Example</a>
  }];

  render() {
    const { activeWorkspace, applicationForm, applicationFormChanged, createApplication, creating } = this.props;
    return (
    <Card
      style={{ marginTop: 15 }}
      title="Applications"
      description="Use topics to stage data coming into and leaving your workspace databases"
      actions={[<a href="#" onClick={this.showNewModal}>New Application</a>]}>
        <Table
          bordered
          pagination={false}
          visible={this.state.visible}
          columns={this.columns}
          dataSource={activeWorkspace.topics} />
        <Modal
          title="Create a Console Producer"
          visible={this.state.visible}
          onCancel={() => this.setState({ visible: false })}
          footer={[<Button onClick={() => this.setState({ visible: false })}>OK</Button>]}>
          <CodeHelp poolName={this.state.selectedTopic} />
        </Modal>
        <Modal
          title="Add a New Application"
          visible={this.state.createVisible}
          onCancel={() => this.setState({ createVisible: false })}>
          <ApplicationForm
            creating={creating}
            createApplication={createApplication}
            onChange={applicationFormChanged}
            workspace={activeWorkspace}
            applicationForm={applicationForm} />
        </Modal>
      </Card>
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
