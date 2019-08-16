import * as React from 'react';
import { Form, Input, Select, Row, Col, Button } from 'antd';
import { RouteComponentProps } from 'react-router-dom';

import { OpenType } from '../constants';
import { ComplianceType } from '../../../constants';
import { ComplianceContent, Question } from '../../../models/Manage';
import { FormComponentProps } from 'antd/lib/form';
import QuestionWrapper from './QuestionWrapper';
import NewQuestion from './NewQuestion';

interface Props extends FormComponentProps<any>, RouteComponentProps<any> {
  openFor: OpenType;
  compliances: ComplianceContent[];
  selectedCompliance: ComplianceContent;

  setRequest: (compliance: ComplianceContent | any) => void;
  setQuestion: (id: number, date: Date, question: string, requester: string) => void;
  addNewQuestion: (question: Question) => void;
  removeQuestion: (id: number) => void;
}

export const formLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 4 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 20 },
    lg: { span: 16 },
  },
};

class ComplianceDetails extends React.Component<Props> {
  public constructor(props: Props) {
    super(props);

    this.handleChange = this.handleChange.bind(this);
    this.handleSelectChange = this.handleSelectChange.bind(this);
  }

  public componentDidMount() {
    const { openFor, compliances, match, setRequest, history } = this.props;

    if (openFor === OpenType.Update) {
      const id = parseInt(match.params.id, 10);
      const selectedCompliance = compliances.find(compliance => compliance.id === id);
      if (!!selectedCompliance) {
        setRequest(selectedCompliance);
      } else {
        history.push('/manage');
      }
    }
  }

  public handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const { selectedCompliance, setRequest } = this.props;
    setRequest({ ...selectedCompliance, [e.target.name]: e.target.value });
  }

  public handleSelectChange(value: ComplianceType) {
    const { selectedCompliance, setRequest } = this.props;
    setRequest({ ...selectedCompliance, name: value });
  }

  public render() {
    const { selectedCompliance, setQuestion, openFor, addNewQuestion, removeQuestion } = this.props;
    const { getFieldDecorator } = this.props.form;
    return (
      <div style={{ textAlign: 'left' }} className="question-details">
        <Row style={{ marginBottom: 15 }}>
          <Col
            xs={{ span: 24, offset: 0 }}
            sm={{ span: 20, offset: 4 }}
            lg={{ span: 16, offset: 4 }}
            style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}
          >
            <h3 style={{ fontSize: 24, fontWeight: 300, marginBottom: 0 }}>Group Details</h3>
            <div>
              <Button type="primary" style={{ marginRight: 20 }} onClick={() => this.props.history.push('/manage')}>
                Cancel
              </Button>
              {openFor === OpenType.Update ? (
                <Button type="primary">Update</Button>
              ) : (
                <Button type="primary">Add</Button>
              )}
            </div>
          </Col>
        </Row>
        <Form {...formLayout}>
          <Form.Item label="Name">
            {getFieldDecorator('name', {
              rules: [{ required: true, message: 'Name is required!' }],
              initialValue: selectedCompliance.name,
            })(
              <Select
                onChange={this.handleSelectChange}
                style={{ width: 200, display: 'block', textTransform: 'uppercase' }}
              >
                <Select.Option value={ComplianceType.PCI} style={{ textTransform: 'uppercase' }}>
                  {ComplianceType.PCI}
                </Select.Option>
                <Select.Option value={ComplianceType.PHI} style={{ textTransform: 'uppercase' }}>
                  {ComplianceType.PHI}
                </Select.Option>
                <Select.Option value={ComplianceType.PII} style={{ textTransform: 'uppercase' }}>
                  {ComplianceType.PII}
                </Select.Option>
              </Select>
            )}
          </Form.Item>
          <Form.Item label="Description">
            {getFieldDecorator('description', {
              rules: [{ required: true, message: 'Description is required!' }],
              initialValue: selectedCompliance.description,
            })(<Input name="description" onChange={this.handleChange} />)}
          </Form.Item>
          <Form.Item label="Questions" />

          {!!selectedCompliance &&
            !!selectedCompliance.questions &&
            selectedCompliance.questions.map((item, index) => (
              <QuestionWrapper
                question={item.question}
                setQuestion={setQuestion}
                id={index}
                key={index}
                removeQuestion={removeQuestion}
              />
            ))}
        </Form>
        <NewQuestion addNewQuestion={addNewQuestion} />
      </div>
    );
  }
}

export default Form.create<Props>()(ComplianceDetails);
