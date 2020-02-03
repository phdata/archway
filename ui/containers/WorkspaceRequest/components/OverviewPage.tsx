import { Col, Form, Input } from 'antd';
import * as React from 'react';
import { FieldLabel } from '../../../components';
import { RequestInput } from '../../../models/RequestInput';
import { FormComponentProps } from 'antd/lib/form/Form';

interface Props extends FormComponentProps {
  request: RequestInput;
  setRequest: (request: RequestInput | boolean) => void;
}

class OverviewPage extends React.Component<Props> {
  public onChangeValue = (key: string) => (e: any) => {
    const { request, setRequest } = this.props;
    setRequest({
      ...request,
      [key]: e.target.value.trim().length ? e.target.value : '',
    });
  };

  public render() {
    const { request, form } = this.props;
    const { getFieldDecorator, isFieldTouched, getFieldError } = form;
    const nameError = isFieldTouched('name') && getFieldError('name');
    const summaryError = isFieldTouched('summary') && getFieldError('summary');
    const descriptionError = isFieldTouched('description') && getFieldError('description');

    return (
      <Col span={12} offset={6}>
        <Form layout="vertical">
          <FieldLabel>Workspace Name</FieldLabel>
          <Form.Item style={{ marginTop: 8 }} validateStatus={nameError ? 'error' : ''}>
            {getFieldDecorator('name', {
              rules: [{ required: true, message: 'Please enter your workspace name!' }],
            })(
              <Input
                size="large"
                name="name"
                placeholder="Loan Modification Group (LMG)"
                value={request.name}
                onChange={this.onChangeValue('name')}
              />
            )}
          </Form.Item>
          <FieldLabel>Summary</FieldLabel>
          <Form.Item style={{ marginTop: 8 }} validateStatus={summaryError ? 'error' : ''}>
            {getFieldDecorator('summary', {
              rules: [{ required: true, message: `Please enter your summary!` }],
            })(
              <Input
                size="large"
                name="summary"
                placeholder="LMG's place to evaluate modification algorithms"
                value={request.summary}
                onChange={this.onChangeValue('summary')}
              />
            )}
          </Form.Item>
          <FieldLabel>Purpose for workspace</FieldLabel>
          <Form.Item style={{ marginTop: 8 }} validateStatus={descriptionError ? 'error' : ''}>
            {getFieldDecorator('description', {
              rules: [{ required: true, message: `Please enter your description!` }],
            })(
              <Input.TextArea
                name="description"
                /* tslint:disable:max-line-length */
                placeholder="(Use this area to fully describe to reviewers and future users why this workspace is needed.)"
                rows={5}
                value={request.description}
                onChange={this.onChangeValue('description')}
              />
            )}
          </Form.Item>
        </Form>
      </Col>
    );
  }
}

export default Form.create<Props>()(OverviewPage);
