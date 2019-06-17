import * as React from 'react';
import { AutoComplete, Row, Radio } from 'antd';
import { InjectedFormProps } from 'redux-form';
import { Field } from 'redux-form/immutable';
import { reduxForm } from 'redux-form/immutable';
import FieldLabel from './FieldLabel';
import { UserSuggestions, UserSuggestion } from './../../../../models/Workspace';

/* tslint:disable:no-var-requires */
const { createComponent, customMap } = require('redux-form-antd');

const RadioGroup = ({ options, onChange, defaultValue }: any) => (
  <Radio.Group defaultValue={defaultValue} buttonStyle="solid" onChange={onChange}>
    {options.map((option: any) => (
      <Radio.Button key={option.value} value={option.value}>
        {option.label}
      </Radio.Button>
    ))}
  </Radio.Group>
);

const RadioField = createComponent(
  RadioGroup,
  customMap((mapProps: any, { input: { onChange } }: any) => ({
    ...mapProps,
    onChange: (e: any) => onChange(e.target.value),
  }))
);

const ReduxAutoComplete = createComponent(
  AutoComplete,
  customMap((mapProps: any, { input: { onChange } }: any) => ({
    ...mapProps,
    onChange: (v: any) => onChange(v),
  }))
);

interface SimpleTopicMemberForm {
  username: string;
  role: string;
}

interface SimpleTopicMemberRequestProps {
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

const SimpleTopicMemberRequest = ({
  suggestions,
  onSearch,
  handleSubmit,
}: InjectedFormProps<SimpleTopicMemberForm, {}> & SimpleTopicMemberRequestProps) => (
  <form style={{}} onSubmit={handleSubmit}>
    <FieldLabel>member</FieldLabel>
    <Field
      name="username"
      dataSource={
        suggestions
          ? [
              renderGroup({
                title: 'Users',
                children: suggestions.users.map((item: UserSuggestion) => ({
                  text: item.display,
                  value: item.distinguished_name,
                })),
              }),
              renderGroup({
                title: 'Groups',
                children: suggestions.groups.map((item: UserSuggestion) => ({
                  text: item.display,
                  value: item.distinguished_name,
                })),
              }),
            ]
          : []
      }
      onSearch={onSearch}
      component={ReduxAutoComplete}
      style={{ marginBottom: 0 }}
    />
    <Row type="flex" align="middle" justify="center" style={{ marginBottom: 0 }}>
      <Field
        defaultValue="readonly"
        name="role"
        options={[
          {
            label: 'Manager',
            value: 'manager',
          },
          {
            label: 'Read Only',
            value: 'readonly',
          },
        ]}
        component={RadioField}
      />
    </Row>
  </form>
);

export default reduxForm<SimpleTopicMemberForm, SimpleTopicMemberRequestProps>({
  form: 'simpleTopicMemberRequest',
  initialValues: {
    username: '',
    role: 'readonly',
  },
})(SimpleTopicMemberRequest);
