import * as React from 'react';
import { Form, Button, Input } from 'antd';
import { FormComponentProps } from 'antd/lib/form';

import { Question } from '../../../models/Manage';
import { formLayout } from './ComplianceDetails';

interface Props extends FormComponentProps {
  children?: any;
  addNewQuestion: (question: Question) => void;
}

const NewQuestion = ({ addNewQuestion, form }: Props) => {
  const [value, setValue] = React.useState('');

  const handleClick = () => {
    form.validateFields(err => {
      if (err) {
        return;
      }
      const newQuestion = {
        question: value,
        requester: 'tony',
        date: new Date(),
      };
      addNewQuestion(newQuestion);
      form.resetFields();
    });
  };

  const { getFieldDecorator } = form;
  return (
    <Form onSubmit={handleClick} {...formLayout}>
      <Form.Item label="Add New">
        {getFieldDecorator('newQuestion', {
          rules: [{ required: true, message: 'To add a new question, type a question in the box' }],
        })(
          <Input
            placeholder="Insert a new question"
            style={{ marginRight: 10 }}
            onChange={e => setValue(e.target.value)}
          />
        )}
        <Button type="primary" icon="plus" shape="circle" onClick={handleClick} size="large" />
      </Form.Item>
    </Form>
  );
};

export default Form.create<Props>()(NewQuestion);
