import * as React from 'react';
import { Form, Button, Input } from 'antd';
import { FormComponentProps } from 'antd/lib/form';

import { Question, ComplianceContent } from '../../../models/Manage';
import { formLayout } from './ComplianceDetails';

interface Props extends FormComponentProps {
  children?: any;
  requester: string;
  compliance: ComplianceContent;
  hasComplianceGroupId: boolean;

  addNewQuestion: (question: Question) => void;
}

const NewQuestion = ({ addNewQuestion, form, requester, hasComplianceGroupId, compliance }: Props) => {
  const [value, setValue] = React.useState('');

  const handleClick = () => {
    form.validateFields(err => {
      if (err) {
        return;
      }
      let newQuestion: Question = {
        question: value,
        requester,
        updated: new Date(),
      };
      if (hasComplianceGroupId) {
        newQuestion = { ...newQuestion, complianceGroupId: compliance.id };
      }
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
