import React from 'react';
import {
  Form,
  Input,
  Select,
  Button,
  List,
} from 'antd';
import { connect } from 'react-redux';

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
}) => {
  const prefixSelector = getFieldDecorator('database', {
    initialValue: workspace.data[0].name
  })(
    <Select>
      { workspace.data.map(db => <Select.Option value={db.name}>{db.name}</Select.Option>) }
    </Select>
  );
  return (
    <Form layout="inline" onSubmit={e =>

      e.preventDefault()
    }>
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
        <Button type="primary">Create</Button>
      </Form.Item>
    </Form>
  );
});

const Topics = ({ topics, activeWorkspace, topicForm, topicFormChanged, createTopic }) => (
  <div>
    <TopicForm
      createTopic={createTopic}
      onChange={topicFormChanged}
      workspace={activeWorkspace}
      topicForm={topicForm} />
    <hr />
    <List
      dataSource={topics}
      grid={{ gutter: 16, column: 4 }}
      renderItem={item => (
        <List.Item>{item.name}</List.Item>
      )} />
  </div>
);

export default connect(
  state => ({
    ...state.workspaces.topics,
    activeWorkspace: state.workspaces.details.activeWorkspace
  }), {
    topicFormChanged,
    createTopic,
  }
)(Topics);