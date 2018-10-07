import { Checkbox, Col, Form, Input } from 'antd';
import { CheckboxChangeEvent } from 'antd/lib/checkbox';
import { FormikBag, InjectedFormikProps, withFormik } from 'formik';
import * as React from 'react';
import * as Yup from 'yup';
import { FieldLabel } from '../../../components';
import { RequestInput } from '../../../types/RequestInput';

interface Props {
  setRequest: (request: RequestInput) => void;
}

const RequestSchema = Yup.object().shape({
  name: Yup.string().required(),
  summary: Yup.string().required(),
  description: Yup.string().required(),
});

class OverviewPage extends React.PureComponent<InjectedFormikProps<Props, RequestInput>> {

    constructor(props: InjectedFormikProps<Props, RequestInput>) {
    super(props);

    this.updateComplianceItem = this.updateComplianceItem.bind(this);
    this.onChangeWrapper = this.onChangeWrapper.bind(this);
  }

    public render() {
    const {
      handleSubmit,
      errors,
      touched,
      values: {
        name,
        summary,
        description,
        compliance: {
          phi_data,
          pii_data,
          pci_data,
        },
      } } = this.props;

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
              /* tslint:disable:max-line-length */
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

    private updateComplianceItem(e: CheckboxChangeEvent) {
    this.props.setFieldValue(`compliance.${e.target.name!}`, e.target.checked);
    this.props.submitForm();
  }

    private onChangeWrapper(e: any) {
    this.props.handleChange(e);
    this.props.submitForm();
  }
}

export default withFormik({
  validationSchema: RequestSchema,
  handleSubmit: (payload: RequestInput, { props }: FormikBag<Props, RequestInput>) => {
    props.setRequest(payload);
  },
  mapPropsToValues: () => ({
    name: '',
    summary: '',
    description: '',
    compliance: {
      phi_data: false,
      pii_data: false,
      pci_data: false,
    },
  }),
})(OverviewPage);
