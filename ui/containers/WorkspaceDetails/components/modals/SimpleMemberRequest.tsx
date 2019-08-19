import * as React from 'react';
import { Row, Radio } from 'antd';
import { InjectedFormProps } from 'redux-form';
import { Field } from 'redux-form/immutable';
import { reduxForm } from 'redux-form/immutable';
import FieldLabel from '../../../../components/FieldLabel';
import { UserSuggestions, HiveAllocation } from './../../../../models/Workspace';
import { MemberForm } from '../../../../models/Manage';
import MemberSearchBar from '../MemberSearchBar';
import { ShowTypes } from '../../constants';

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

interface SimpleMemberRequestProps {
  allocations: HiveAllocation[];
  suggestions?: UserSuggestions;
  loading: boolean;
  onSearch?: (v: string) => void;
  handleSubmit?: () => void;
}

const SimpleMemberRequest = ({
  allocations,
  suggestions,
  loading,
  onSearch,
  handleSubmit,
}: InjectedFormProps<MemberForm, {}> & SimpleMemberRequestProps) => (
  <form onSubmit={handleSubmit}>
    <MemberSearchBar
      loading={loading}
      suggestions={suggestions}
      showTypes={[ShowTypes.Users, ShowTypes.Groups]}
      onSearch={onSearch}
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

export default reduxForm<MemberForm, SimpleMemberRequestProps>({
  form: 'simpleMemberRequest',
  initialValues: {
    username: '',
    roles: {},
  },
})(SimpleMemberRequest);
