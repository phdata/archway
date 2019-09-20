import * as React from 'react';
import { Input, Form, Button } from 'antd';
import { Question } from '../../../models/Manage';
import { WrappedFormUtils } from 'antd/lib/form/Form';

interface Props {
  question: Question;
  children?: any;
  index: number;
  form: WrappedFormUtils<any>;

  removeQuestion: (index: number) => void;
  setQuestion: (index: number, question: Question) => void;
}

class QuestionWrapper extends React.Component<Props> {
  public constructor(props: Props) {
    super(props);

    this.handleChange = this.handleChange.bind(this);
    this.handleRemove = this.handleRemove.bind(this);
  }

  public handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const { index, setQuestion } = this.props;
    const question: Question = {
      ...this.props.question,
      question: e.target.value,
      updated: new Date(),
    };
    setQuestion(index, question);
  }

  public handleRemove(index: number) {
    this.props.form.resetFields([`question${index}`]);
    this.props.removeQuestion(index);
  }

  public render() {
    const { index, question } = this.props;
    const { getFieldDecorator } = this.props.form;
    return (
      <Form.Item label={`${index + 1}`}>
        {getFieldDecorator(`question${index}`, {
          rules: [{ required: true, message: 'Fill in the question!' }],
          initialValue: question.question,
        })(<Input name={`question${index}`} onChange={this.handleChange} style={{ marginRight: 10 }} />)}
        <Button type="primary" shape="circle" icon="minus" size="large" onClick={() => this.handleRemove(index)} />
      </Form.Item>
    );
  }
}

export default QuestionWrapper;
