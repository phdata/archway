import * as React from 'react';
import { Col, Form, Input, Checkbox } from 'antd';
import { withFormik, InjectedFormikProps, FormikFormProps, FormikBag } from 'formik';
import FieldLabel from '../../Common/FieldLabel';
import { Request } from './model';
import { CheckboxChangeEvent } from 'antd/lib/checkbox';
import * as Yup from 'yup';

interface Props {
  setRequest: (request: Request) => void
}

const RequestSchema = Yup.object().shape({
  name: Yup.string().required(),
  summary: Yup.string().required(),
  description: Yup.string().required(),
});

class OverviewPage extends React.PureComponent<InjectedFormikProps<Props, Request>> {

  constructor(props: InjectedFormikProps<Props, Request>) {
    super(props);

    this.updateComplianceItem = this.updateComplianceItem.bind(this);
    this.onChangeWrapper = this.onChangeWrapper.bind(this);
  }

  updateComplianceItem(e: CheckboxChangeEvent) {
    this.props.setFieldValue(e.target.name!, e.target.checked);
    this.props.submitForm();
  }

  onChangeWrapper(e: any) {
    this.props.handleChange(e);
    this.props.submitForm();
  }

  render() {
    const {
      handleSubmit,
      errors,
      touched,
      values: {
        name,
        summary,
        description,
        phi_data,
        pii_data,
        pci_data
      } } = this.props;
      console.log(errors);
    return (
      <Col span={12} offset={6}>
        <Form
          layout="vertical"
          onSubmit={handleSubmit}>
          <Form.Item
            validateStatus={errors.name && touched.name ? 'error' : 'success'}
            label={<FieldLabel>Workspace Name</FieldLabel>}>
            <Input
              size="large"
              name="name"
              placeholder="Loan Modification Group (LMG)"
              value={name}
              onChange={this.onChangeWrapper} />
          </Form.Item>
          <Form.Item
            validateStatus={errors.summary && touched.summary ? 'error' : 'success'}
            label={<FieldLabel>Summary</FieldLabel>}>
            <Input
              size="large"
              name="summary"
              placeholder="LMG's place to evaluate modification algorithms"
              value={summary}
              onChange={this.onChangeWrapper} />
          </Form.Item>
          <Form.Item
            validateStatus={errors.description && touched.description ? 'error' : 'success'}
            label={<FieldLabel>Purpose for workspace</FieldLabel>}>
            <Input.TextArea
              name="description"
              placeholder="(Use this area to fully describe to reviewers and future users why this workspace is needed.)"
              rows={5}
              value={description}
              onChange={this.onChangeWrapper} />
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
  validationSchema: RequestSchema,
  handleSubmit: (payload: Request, { props }: FormikBag<Props, Request>) => {
    props.setRequest(payload);
  },
  mapPropsToValues: () => ({
    name: '',
    summary: '',
    description: '',
    phi_data: false,
    pii_data: false,
    pci_data: false,
  })
})(OverviewPage);;