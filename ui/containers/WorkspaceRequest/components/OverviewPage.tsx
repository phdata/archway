import { Col, Form, Input } from 'antd';
import * as React from 'react';
import { FieldLabel } from '../../../components';
import { RequestInput } from '../../../models/RequestInput';

interface Props {
  request: RequestInput;
  setRequest: (request: RequestInput | boolean) => void;
}

export default class OverviewPage extends React.Component<Props> {
  public onChangeValue = (key: string) => (e: any) => {
    const { request, setRequest } = this.props;
    setRequest({
      ...request,
      [key]: e.target.value,
    });
  };

  public render() {
    const { request } = this.props;

    return (
      <Col span={12} offset={6}>
        <Form layout="vertical">
          <Form.Item label={<FieldLabel>Workspace Name</FieldLabel>}>
            <Input
              size="large"
              name="name"
              placeholder="Loan Modification Group (LMG)"
              value={request.name}
              onChange={this.onChangeValue('name')}
            />
          </Form.Item>
          <Form.Item label={<FieldLabel>Summary</FieldLabel>}>
            <Input
              size="large"
              name="summary"
              placeholder="LMG's place to evaluate modification algorithms"
              value={request.summary}
              onChange={this.onChangeValue('summary')}
            />
          </Form.Item>
          <Form.Item label={<FieldLabel>Purpose for workspace</FieldLabel>}>
            <Input.TextArea
              name="description"
              /* tslint:disable:max-line-length */
              placeholder="(Use this area to fully describe to reviewers and future users why this workspace is needed.)"
              rows={5}
              value={request.description}
              onChange={this.onChangeValue('description')}
            />
          </Form.Item>
        </Form>
      </Col>
    );
  }
}
