import { Checkbox, Col, Form, Input } from 'antd';
import * as React from 'react';
import { FieldLabel } from '../../../components';
import { RequestInput } from '../../../models/RequestInput';

interface Props {
  request: RequestInput,
  setRequest: (request: RequestInput | boolean) => void;
}

export default class OverviewPage extends React.Component<Props> {
  public onChangeValue = (key: string) => (e: any) => {
    const { request, setRequest } = this.props;
    setRequest({
      ...request,
      [key]: e.target.value,
    });
  }

  public onChangeCheckbox = (key: string) => (e: any) => {
    const { request, setRequest } = this.props;
    setRequest({
      ...request,
      compliance: {
        ...request.compliance,
        [key]: e.target.checked,
      },
    });
  }

  public render() {
    const { request } = this.props;

    return (
      <Col span={12} offset={6}>
        <Form layout="vertical">
          <Form.Item
            label={<FieldLabel>Workspace Name</FieldLabel>}>
            <Input
              size="large"
              name="name"
              placeholder="Loan Modification Group (LMG)"
              value={request.name}
              onChange={this.onChangeValue('name')}
            />
          </Form.Item>
          <Form.Item
            label={<FieldLabel>Summary</FieldLabel>}>
            <Input
              size="large"
              name="summary"
              placeholder="LMG's place to evaluate modification algorithms"
              value={request.summary}
              onChange={this.onChangeValue('summary')}
            />
          </Form.Item>
          <Form.Item
            label={<FieldLabel>Purpose for workspace</FieldLabel>}>
            <Input.TextArea
              name="description"
              /* tslint:disable:max-line-length */
              placeholder="(Use this area to fully describe to reviewers and future users why this workspace is needed.)"
              rows={5}
              value={request.description}
              onChange={this.onChangeValue('description')}
            />
          </Form.Item>
          <Form.Item
            label={<FieldLabel>The data in this workspace may contain</FieldLabel>}>
            <Checkbox
              name="phi_data"
              checked={request.compliance.phi_data}
              onChange={this.onChangeCheckbox('phi_data')}
            >
              PHI
            </Checkbox>
            <Checkbox
              name="pci_data"
              checked={request.compliance.pci_data}
              onChange={this.onChangeCheckbox('pci_data')}
            >
              PCI
            </Checkbox>
            <Checkbox
              name="pii_data"
              checked={request.compliance.pii_data}
              onChange={this.onChangeCheckbox('pii_data')}
            >
              PII
            </Checkbox>
          </Form.Item>
        </Form>
      </Col>
    );
  }
}
