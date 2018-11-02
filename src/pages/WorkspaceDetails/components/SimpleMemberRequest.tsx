import * as React from 'react';
import { AutoComplete } from 'antd';
import { InjectedFormProps } from 'redux-form';
import { Field } from 'redux-form/immutable';
import { reduxForm } from 'redux-form/immutable';
import FieldLabel from '../../../components/FieldLabel';

/* tslint:disable:no-var-requires */
const { createComponent, customMap } = require('redux-form-antd');

const ReduxAutoComplete = createComponent(AutoComplete, customMap((mapProps: any, { input: { onChange } }: any) => ({
  ...mapProps,
  onChange: (v: any) => onChange(v),
})));

interface SimpleMemberForm {
  username: string;
}

interface SimpleMemberRequestProps {
  suggestions?: string[];
  onSearch?: (v: string) => void;
}

const SimpleMemberRequest = ({
  suggestions,
  onSearch,
  handleSubmit,
}: InjectedFormProps<SimpleMemberForm, {}> & SimpleMemberRequestProps) => (
  <form style={{  }} onSubmit={handleSubmit}>
    <FieldLabel>Username</FieldLabel>
    <Field
      name="username"
      dataSource={suggestions || []}
      onSearch={onSearch}
      component={ReduxAutoComplete}
      style={{ marginBottom: 0 }}
    />
  </form>
);

export default reduxForm<SimpleMemberForm, SimpleMemberRequestProps>({
  form: 'simpleMemberRequest',
})(SimpleMemberRequest);
