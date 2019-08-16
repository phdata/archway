import * as React from 'react';
import { Input, Form, Button } from 'antd';
import { FormComponentProps } from 'antd/lib/form';

interface Props extends FormComponentProps {
  question: string;
  children?: any;
  id: number;

  removeQuestion: (id: number) => void;
  setQuestion: (id: number, date: Date, question: string, requester: string) => void;
}

class Question extends React.Component<Props> {
  public handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { id, setQuestion } = this.props;
    setQuestion(id, new Date(), e.target.value, 'tony');
  };
  public render() {
    const { id, question, removeQuestion } = this.props;
    const { getFieldDecorator } = this.props.form;
    return (
      <Form.Item label={`${id + 1}`}>
        {getFieldDecorator(`question${id}`, {
          rules: [{ required: true, message: 'Fill in the question!' }],
          initialValue: question,
        })(<Input name={`question${id}`} onChange={this.handleChange} style={{ marginRight: 10 }} />)}
        <Button type="primary" shape="circle" icon="minus" size="large" onClick={() => removeQuestion(id)} />
      </Form.Item>
    );
  }
}

export default Form.create<Props>()(Question);
