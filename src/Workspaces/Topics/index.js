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
      { workspace.data.map(db => <Select.Option key={db.id} value={db.name}>{db.name}</Select.Option>) }
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

const TopicItem = ({ topic }) => {
  const consumer = `$ bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --from-beginning --topic ${topic.name}`;
  const producer = `$ bin/kafka-console-producer.sh --broker-list localhost:9092 --topic ${topic.name}`;
  return (
    <div>
      <h2>Create a producer in one shell:</h2>
      <SyntaxHighlighter language="shell" style={solarizedDark}>
        {producer}
      </SyntaxHighlighter>
      <h2>Create a consumer in another shell:</h2>
      <SyntaxHighlighter language="shell" style={solarizedDark}>
        {consumer}
      </SyntaxHighlighter>
    </div>
  );
}

class Topics extends Component {

  render() {
    const { topics, activeWorkspace, topicForm, topicFormChanged, createTopic, creating, } = this.props;
    topicFormChanged({ database: { value: activeWorkspace.data[0].name } });
    return (
      <Row>
        <Col span={16}>
          <Tabs>
            {activeWorkspace.topics.map(topic => (
              <Tabs.TabPane tab={topic.name} key={topic.name}>
                <TopicItem topic={topic} />
              </Tabs.TabPane>
            ))}
          </Tabs>
        </Col>
        <Col offset={1} span={6}>
          <h3>Create a new topic:</h3>
          <TopicForm
            creating={creating}
            createTopic={createTopic}
            onChange={topicFormChanged}
            workspace={activeWorkspace}
            topicForm={topicForm} />
        </Col>
      </Row>
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