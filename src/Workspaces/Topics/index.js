import React, { Component } from 'react';
import {
  Row,
  Col,
  Form,
  Input,
  Select,
  Button,
  Tabs,
  Table,
  Modal,
  Card,
} from 'antd';
import { connect } from 'react-redux';
import SyntaxHighlighter from 'react-syntax-highlighter';
import { solarizedDark } from 'react-syntax-highlighter/styles/hljs';

import ValueDisplay from '../ValueDisplay';
import { topicFormChanged, createTopic } from './actions';

const TopicForm = Form.create({
  onFieldsChange(props, changedFields) {
    props.onChange(changedFields);
  },
  mapPropsToFields(props) {
    return {
      database: Form.createFormField({ value: props.topicForm.database }),
      suffix: Form.createFormField({ value: props.topicForm.suffix }),
      partitions: Form.createFormField({ value: props.topicForm.partitions }),
      replicationFactor: Form.createFormField({ value: props.topicForm.replicationFactor }),
    };
  }
})(({
  form: {
    getFieldDecorator
  },
  workspace,
  createTopic,
  creating,
}) => {
  const prefixSelector = getFieldDecorator('database', {})(
    <Select>
      { workspace.data.map(db => <Select.Option key={db.id} value={db.name}>{db.name}.</Select.Option>) }
    </Select>
  );
  return (
    <Form onSubmit={e => {
      createTopic();
      e.preventDefault();
    }}>
      <Form.Item>
        {getFieldDecorator('suffix', {
            rules: [{ required: true, message: 'please provide a topic name' }],
          })(<Input placeholder="topic suffix" size="large" addonBefore={prefixSelector} />)}
      </Form.Item>
      <Form.Item>
        {getFieldDecorator('partitions', {
            rules: [{ required: true, message: 'please provide number of partitions' }],
          })(<Input placeholder="partitions" size="large" />)}
      </Form.Item>
      <Form.Item>
        {getFieldDecorator('replicationFactor', {
            rules: [{ required: true, message: 'please provide the replication factor' }],
          })(<Input placeholder="replication factor" size="large" />)}
      </Form.Item>
      <Form.Item>
        <Button loading={creating} htmlType="submit" size="large" type="primary" style={{ width: '100%' }}>Create</Button>
      </Form.Item>
    </Form>
  );
});

const CodeHelp = ({ topic }) => {
  const producer = `$ bin/kafka-console-producer.sh --broker-list localhost:9092 --topic ${topic.name}`;
  return (
    <SyntaxHighlighter language="shell" style={solarizedDark}>
      {producer}
    </SyntaxHighlighter>
  );
}

class Topics extends React.Component {

  state = {
    selectedTopic: false,
    visible: false,
    createVisible: false,
  }

  showModal = (selectedTopic) => {
    this.setState({
      selectedTopic,
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
    title: 'Partitions',
    dataIndex: 'partitions',
  }, {
    title: 'Replication Factor',
    dataIndex: 'replication_factor',
  }, {
    title: 'Help',
    render: (text, record) => <a href="#" onClick={() => this.showModal(record.name)}>Example</a>
  }];

  render() {
    const { activeWorkspace, topicForm, topicFormChanged, createTopic, creating } = this.props;
    return (
    <Card
      style={{ marginTop: 15 }}
      title="Topics"
      description="Use topics to stage data coming into and leaving your workspace databases"
      actions={[<a href="#" onClick={this.showNewModal}>New Topic</a>]}>
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
          title="Add a New Topic"
          visible={this.state.createVisible}
          onCancel={() => this.setState({ createVisible: false })}>
          <TopicForm
            creating={creating}
            createTopic={createTopic}
            onChange={topicFormChanged}
            workspace={activeWorkspace}
            topicForm={topicForm} />
        </Modal>
      </Card>
    );
  }

}

export default connect(
  state => ({
    ...state.workspaces.topics,
    activeWorkspace: state.workspaces.details.activeWorkspace
  }), {
    topicFormChanged,
    createTopic,
  }
)(Topics);
