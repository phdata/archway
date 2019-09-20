import * as React from 'react';
import { Form, Input, Select, Row, Col, Button } from 'antd';
import { RouteComponentProps } from 'react-router-dom';

import { OpenType, ManagePage, formLayout } from '../constants';
import { ComplianceType } from '../../../constants';
import { ComplianceContent, Question } from '../../../models/Manage';
import { FormComponentProps } from 'antd/lib/form';
import QuestionWrapper from './QuestionWrapper';
import NewQuestion from './NewQuestion';

interface Props extends FormComponentProps<any>, RouteComponentProps<any> {
  openFor: OpenType;
  compliances: ComplianceContent[];
  requester: string;
  selectedCompliance: ComplianceContent;

  setRequest: (compliance: ComplianceContent | any) => void;
  setQuestion: (index: number, question: Question) => void;
  addNewQuestion: (question: Question) => void;
  removeQuestion: (index: number) => void;
  addCompliance: () => void;
  deleteCompliance: () => void;
  updateCompliance: () => void;
}

class ComplianceDetails extends React.Component<Props> {
  public constructor(props: Props) {
    super(props);

    this.handleChange = this.handleChange.bind(this);
    this.handleSelectChange = this.handleSelectChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  public componentDidMount() {
    const { openFor, compliances, match, setRequest, history } = this.props;
    if (openFor === OpenType.Update) {
      const id = parseInt(match.params.id, 10);
      const selectedCompliance = compliances.find(compliance => compliance.id === id);
      if (!!selectedCompliance) {
        setRequest(selectedCompliance);
      } else {
        history.push(`/manage/${ManagePage.ComplianceTab}`);
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

  public handleSubmit(e: React.SyntheticEvent, buttonType: OpenType) {
    const { updateCompliance, deleteCompliance, addCompliance, form } = this.props;
    form.validateFields(err => {
      if (err) {
        return;
      }
      switch (buttonType) {
        case OpenType.Update:
          updateCompliance();
          break;
        case OpenType.Delete:
          deleteCompliance();
          break;
        case OpenType.Add:
          addCompliance();
          break;
      }
    });
  }

  public renderButtons() {
    const { openFor } = this.props;

    return (
      <div>
        <Button
          type="primary"
          style={{ marginRight: 20 }}
          onClick={() => this.props.history.push(`/manage/${ManagePage.ComplianceTab}`)}
        >
          Cancel
        </Button>
        {openFor === OpenType.Update ? (
          <React.Fragment>
            <Button type="primary" style={{ marginRight: 20 }} onClick={e => this.handleSubmit(e, OpenType.Delete)}>
              Delete
            </Button>
            <Button type="primary" onClick={e => this.handleSubmit(e, OpenType.Update)}>
              Update
            </Button>
          </React.Fragment>
        ) : (
          <Button type="primary" onClick={e => this.handleSubmit(e, OpenType.Add)}>
            Add
          </Button>
        )}
      </div>
    );
  }

  public renderQuestions() {
    const { selectedCompliance, setQuestion, removeQuestion, form } = this.props;
    return (
      !!selectedCompliance &&
      !!selectedCompliance.questions &&
      selectedCompliance.questions.map((item, index) => (
        <QuestionWrapper
          question={item}
          setQuestion={setQuestion}
          index={index}
          key={index}
          removeQuestion={removeQuestion}
          form={form}
        />
      ))
    );
  }

  public render() {
    const { selectedCompliance, addNewQuestion, requester } = this.props;
    const { getFieldDecorator } = this.props.form;

    return (
      <div style={{ textAlign: 'left' }} className="question-details">
        <Form {...formLayout}>
          <Row style={{ marginBottom: 15 }}>
            <Col
              xs={{ span: 24, offset: 0 }}
              sm={{ span: 20, offset: 4 }}
              lg={{ span: 16, offset: 4 }}
              style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}
            >
              <h3 style={{ fontSize: 24, fontWeight: 300, marginBottom: 0 }}>Group Details</h3>
              {this.renderButtons()}
            </Col>
          </Row>
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
          {this.renderQuestions()}
        </Form>
        <NewQuestion addNewQuestion={addNewQuestion} requester={requester} complianceGroupId={selectedCompliance.id} />
      </div>
    );
  }
}

export default Form.create<Props>()(ComplianceDetails);
