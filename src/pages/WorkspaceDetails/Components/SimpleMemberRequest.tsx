import * as React from 'react';
import { InjectedFormProps } from 'redux-form';
import { Field } from 'redux-form/immutable';
import { reduxForm } from 'redux-form/immutable';
import FieldLabel from '../../../components/FieldLabel';

/* tslint:disable:no-var-requires */
const { TextField } = require('redux-form-antd');

interface SimpleMemberForm {
  username: string;
}

const SimpleMemberRequest = ({ handleSubmit }: InjectedFormProps<SimpleMemberForm, {}>) => (
  <form style={{  }} onSubmit={handleSubmit}>
    <FieldLabel>Username</FieldLabel>
    <Field name="username" component={TextField} style={{ marginBottom: 0 }} />
  </form>
);

export default reduxForm<SimpleMemberForm, {}>({
  form: 'simpleMemberRequest',
})(SimpleMemberRequest);
