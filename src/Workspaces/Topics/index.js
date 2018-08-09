import React, { Component } from 'react';
import {
  Row,
  Col,
  Form,
  Input,
  Select,
  Button,
  Tabs,
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

const TopicItem = ({ topic }) => {
  const producer = `$ bin/kafka-console-producer.sh --broker-list localhost:9092 --topic ${topic.name}`;
  return (
    <div>
      <Row type="flex" justify="center">
        <Col span={12}>
          <ValueDisplay label="name">
            {topic.name}
          </ValueDisplay>
        </Col>
        <Col span={6}>
          <ValueDisplay label="partitions">
            {topic.partitions}
          </ValueDisplay>
        </Col>
        <Col span={6}>
          <ValueDisplay label="replication factor">
            {topic.replication_factor}
          </ValueDisplay>
        </Col>
      </Row>
      <h2>Start sending messages via a console producer:</h2>
      <SyntaxHighlighter language="shell" style={solarizedDark}>
        {producer}
      </SyntaxHighlighter>
    </div>
  );
}

class Topics extends Component {

  render() {
    const { activeWorkspace, topicForm, topicFormChanged, createTopic, creating, } = this.props;
    topicFormChanged({ database: { value: activeWorkspace.data[0].name } });
    return (
      <Row>
        <Col span={16}>
          {activeWorkspace.topics.length === 0 && (
            <h2>No topics yet. Create one using the form to the right.</h2>
          )}
          {activeWorkspace.topics.length === 1 && <TopicItem topic={activeWorkspace.topics[0]} />}
          {activeWorkspace.topics.length > 1 && (
            <Tabs>
              {activeWorkspace.topics.map(topic => (
                <Tabs.TabPane tab={topic.name} key={topic.name}>
                  <TopicItem topic={topic} />
                </Tabs.TabPane>
              ))}
            </Tabs>
          )}
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