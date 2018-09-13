import * as React from 'react';
import {Col, Form, Input, Checkbox} from 'antd';
import {withFormik, InjectedFormikProps, FormikFormProps} from 'formik';
import FieldLabel from '../../Common/FieldLabel';
import {Request} from './model';

const OverviewPage = ({ 
  handleChange, 
  handleSubmit, 
  values: { 
    name, 
    summary,
    description,
    phi_data,
    pii_data,
    pci_data
  } }: InjectedFormikProps<FormikFormProps, Request>) => (
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
        <Checkbox.Group onChange={handleChange}>
          <Checkbox name="compliance" value="phi_data">PHI</Checkbox>
          <Checkbox name="compliance" value="pci_data">PCI</Checkbox>
          <Checkbox name="compliance" value="pii_data">PII</Checkbox>
        </Checkbox.Group>
      </Form.Item>
    </Form>
  </Col>
);

export default withFormik({
  handleSubmit: (payload, { props }: any) => {
    props.setRequest(payload);
  }
})(OverviewPage);;