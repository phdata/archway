import * as React from 'react';
import { Col, Form, Input, Checkbox } from 'antd';
import { withFormik, InjectedFormikProps, FormikFormProps } from 'formik';
import FieldLabel from '../../Common/FieldLabel';
import { Request } from './model';
import { CheckboxChangeEvent } from 'antd/lib/checkbox';

class OverviewPage extends React.PureComponent<InjectedFormikProps<FormikFormProps, Request>> {

  constructor(props: InjectedFormikProps<FormikFormProps, Request>) {
    super(props);

    this.updateComplianceItem = this.updateComplianceItem.bind(this);
  }

  updateComplianceItem(e: CheckboxChangeEvent) {
    this.props.setFieldValue(e.target.name!, e.target.checked);
  }

  render() {
    const {
      handleChange,
      handleSubmit,
      values: {
        name,
        summary,
        description,
        phi_data,
        pii_data,
        pci_data
      } } = this.props;
    console.log(this.props.values);
    return (
      <Col span={12} offset={6}>
        <Form
          layout="vertical"
          onSubmit={handleSubmit}>
          <Form.Item
            label={<FieldLabel>Workspace Name</FieldLabel>}>
            <Input
              size="large"
              name="name"
              placeholder="Loan Modification Group (LMG)"
              value={name}
              onChange={handleChange} />
          </Form.Item>
          <Form.Item
            label={<FieldLabel>Summary</FieldLabel>}>
            <Input
              size="large"
              name="summary"
              placeholder="LMG's place to evaluate modification algorithms"
              value={summary}
              onChange={handleChange} />
          </Form.Item>
          <Form.Item
            label={<FieldLabel>Purpose for workspace</FieldLabel>}>
            <Input.TextArea
              name="description"
              placeholder="(Use this area to fully describe to reviewers and future users why this workspace is needed.)"
              rows={5}
              value={description}
              onChange={handleChange} />
          </Form.Item>
          <Form.Item
            label={<FieldLabel>The data in this workspace may contain</FieldLabel>}>
            <Checkbox name="phi_data" checked={phi_data} onChange={this.updateComplianceItem}>PHI</Checkbox>
            <Checkbox name="pci_data" checked={pci_data} onChange={this.updateComplianceItem}>PCI</Checkbox>
            <Checkbox name="pii_data" checked={pii_data} onChange={this.updateComplianceItem}>PII</Checkbox>
          </Form.Item>
        </Form>
      </Col>
    );
  }
}

export default withFormik({
  handleSubmit: (payload, { props }: any) => {
    props.setRequest(payload);
  }
})(OverviewPage);;