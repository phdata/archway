import * as React from 'react';
import { Form } from 'antd';
import { Field, InjectedFormProps } from 'redux-form';
import { reduxForm } from 'redux-form/immutable';

/* tslint:disable:no-var-requires */
const { TextField } = require('redux-form-antd');

interface Props {
  handleSubmit: () => void;
}

interface KafkaForm {
  name: string;
}

const KafkaTopicRequest = ({ handleSubmit }: InjectedFormProps<KafkaForm>) => (
  <Form layout="vertical" onSubmit={handleSubmit}>
    <Field name="name" component={TextField} />
  </Form>
);

export default reduxForm<KafkaForm, {}>({
  form: 'topicRequest',
})(KafkaTopicRequest);
