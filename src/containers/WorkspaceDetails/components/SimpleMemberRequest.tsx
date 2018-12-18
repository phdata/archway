import * as React from 'react';
import { AutoComplete } from 'antd';
import { InjectedFormProps } from 'redux-form';
import { Field } from 'redux-form/immutable';
import { reduxForm } from 'redux-form/immutable';
import FieldLabel from '../../../components/FieldLabel';
import { UserSuggestions, UserSuggestion } from './../../../models/Workspace';

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
  suggestions?: UserSuggestions;
  onSearch?: (v: string) => void;
}

const renderGroup = (group: any) => (
  <AutoComplete.OptGroup
    key={group.title}
    label={<span style={{ fontSize: '14px', fontWeight: 'bold' }}>{group.title}</span>}
  >
    {group.children.map((opt: any) => (
      <AutoComplete.Option key={opt.value} value={opt.value}>
        {opt.text}
      </AutoComplete.Option>
    ))}
  </AutoComplete.OptGroup>
);

const SimpleMemberRequest = ({
  suggestions,
  onSearch,
  handleSubmit,
}: InjectedFormProps<SimpleMemberForm, {}> & SimpleMemberRequestProps) => (
  <form style={{  }} onSubmit={handleSubmit}>
    <FieldLabel>Username</FieldLabel>
    <Field
      name="username"
      dataSource={suggestions ? [
        renderGroup({
          title: 'Users',
          children: suggestions.users.map((item: UserSuggestion) => ({
            text: `${item.display} (${item.display})`,
            value: item.distinguished_name,
          })),
        }),
        renderGroup({
          title: 'Groups',
          children: suggestions.groups.map((item: UserSuggestion) => ({
            text: `${item.display} (${item.display})`,
            value: item.distinguished_name,
          })),
        }),
      ] : []}
      onSearch={onSearch}
      component={ReduxAutoComplete}
      style={{ marginBottom: 0 }}
    />
  </form>
);

export default reduxForm<SimpleMemberForm, SimpleMemberRequestProps>({
  form: 'simpleMemberRequest',
})(SimpleMemberRequest);
