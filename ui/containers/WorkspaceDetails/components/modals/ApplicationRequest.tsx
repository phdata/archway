import * as React from 'react';
import { InjectedFormProps } from 'redux-form';
import { Field } from 'redux-form/immutable';
import { reduxForm } from 'redux-form/immutable';
import FieldLabel from '../../../../components/FieldLabel';

/* tslint:disable:no-var-requires */
const { TextField, SelectField } = require('redux-form-antd');

interface KafkaForm {
  name: string;
}

const ApplicationRequest = ({ handleSubmit }: InjectedFormProps<KafkaForm, {}>) => (
  <form style={{}} onSubmit={handleSubmit}>
    <FieldLabel>Application Name</FieldLabel>
    <Field name="name" component={TextField} />
    <FieldLabel>Application Type</FieldLabel>
    <Field
      name="application_type"
      placeholder="Select Application Type"
      component={SelectField}
      options={[
        {
          value: 'Spark',
          label: 'Spark',
        },
        {
          value: 'Impala',
          label: 'Impala',
        },
        {
          value: 'Kudu',
          label: 'Kudu',
        },
        {
          value: 'Other',
          label: 'Other',
        },
      ]}
    />
    <FieldLabel>Logo URL</FieldLabel>
    <Field name="logo" component={TextField} />
    <FieldLabel>Language</FieldLabel>
    <Field
      name="language"
      placeholder="Select Language"
      component={SelectField}
      options={[
        {
          value: 'Java',
          label: 'Java',
        },
        {
          value: 'Scala',
          label: 'Scala',
        },
        {
          value: 'Python',
          label: 'Python',
        },
        {
          value: 'Other',
          label: 'Other',
        },
      ]}
    />
    <FieldLabel>Repository</FieldLabel>
    <Field name="repository" component={TextField} />
  </form>
);

export default reduxForm<KafkaForm, {}>({
  form: 'applicationRequest',
})(ApplicationRequest);
