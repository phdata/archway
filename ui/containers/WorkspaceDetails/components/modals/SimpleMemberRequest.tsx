import * as React from 'react';
import { AutoComplete, Row, Radio } from 'antd';
import { InjectedFormProps } from 'redux-form';
import { Field } from 'redux-form/immutable';
import { reduxForm } from 'redux-form/immutable';
import FieldLabel from './FieldLabel';
import { UserSuggestions, UserSuggestion, HiveAllocation } from './../../../../models/Workspace';

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

interface SimpleMemberForm {
  username: string;
  roles: object;
}

interface SimpleMemberRequestProps {
  allocations: HiveAllocation[];
  suggestions?: UserSuggestions;
  onSearch?: (v: string) => void;
  handleSubmit?: () => void;
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
  allocations,
  suggestions,
  onSearch,
  handleSubmit,
}: InjectedFormProps<SimpleMemberForm, {}> & SimpleMemberRequestProps) => (
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
    {allocations.map((allocation: HiveAllocation) => {
      return (
        <div key={allocation.name}>
          <FieldLabel>{allocation.name}</FieldLabel>
          <Row type="flex" align="middle" justify="center" style={{ marginBottom: 0 }}>
            <Field
              defaultValue="readonly"
              name={`roles.${allocation.name}`}
              options={[
                {
                  label: 'Manager',
                  value: 'manager',
                },
                {
                  label: 'Read Only',
                  value: 'readonly',
                },
                {
                  label: 'Read/Write',
                  value: 'readwrite',
                },
                ...(allocations.length > 1
                  ? [
                      {
                        label: 'None',
                        value: 'none',
                      },
                    ]
                  : []),
              ]}
              component={RadioField}
            />
          </Row>
        </div>
      );
    })}
  </form>
);

export default reduxForm<SimpleMemberForm, SimpleMemberRequestProps>({
  form: 'simpleMemberRequest',
  initialValues: {
    username: '',
    roles: {},
  },
})(SimpleMemberRequest);
