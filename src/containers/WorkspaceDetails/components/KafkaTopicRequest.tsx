import * as React from 'react';
import { InjectedFormProps } from 'redux-form';
import { Field } from 'redux-form/immutable';
import { reduxForm } from 'redux-form/immutable';
import FieldLabel from '../../../components/FieldLabel';

/* tslint:disable:no-var-requires */
const { TextField } = require('redux-form-antd');

interface KafkaForm {
  name: string;
}

const KafkaTopicRequest = ({ handleSubmit }: InjectedFormProps<KafkaForm, {}>) => (
  <form style={{  }} onSubmit={handleSubmit}>
    <FieldLabel>Topic Name</FieldLabel>
    <Field name="name" component={TextField} style={{ marginBottom: 0 }} />
  </form>
);

export default reduxForm<KafkaForm, {}>({
  form: 'topicRequest',
})(KafkaTopicRequest);
