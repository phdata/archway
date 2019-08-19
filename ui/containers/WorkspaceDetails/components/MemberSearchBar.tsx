import * as React from 'react';
import { AutoComplete, Spin, Icon } from 'antd';
import { Field } from 'redux-form/immutable';

import { FieldLabel } from '../../../components';
import { UserSuggestion, UserSuggestions } from '../../../models/Workspace';
import { ShowTypes } from '../constants';

/* tslint:disable:no-var-requires */
const { createComponent, customMap } = require('redux-form-antd');

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

const options = (showTypes: ShowTypes[], suggestions: UserSuggestions) =>
  showTypes.map(type =>
    renderGroup({
      title: type,
      children: suggestions[type.toLowerCase()].map((item: UserSuggestion) => ({
        text: item.display,
        value: item.distinguished_name,
      })),
    })
  );

const ReduxAutoComplete = createComponent(
  AutoComplete,
  customMap((mapProps: any, { input: { onChange } }: any) => ({
    ...mapProps,
    onChange: (v: any) => onChange(v),
  }))
);

interface Props {
  loading: boolean;
  suggestions?: UserSuggestions;
  showTypes: ShowTypes[];
  onSearch?: (v: string) => void;
}

const MemberSearchBar = ({ loading, suggestions, showTypes, onSearch }: Props) => (
  <div>
    <FieldLabel>MEMBER ID SEARCH</FieldLabel>
    <div style={{ position: 'relative' }}>
      <Field
        name="username"
        dataSource={suggestions ? options(showTypes, suggestions) : []}
        onSearch={onSearch}
        component={ReduxAutoComplete}
        style={{ marginBottom: 0 }}
      />
      {loading && (
        <Spin
          indicator={<Icon type="sync" style={{ fontSize: 20 }} spin />}
          style={{ position: 'absolute', top: 6, right: 6 }}
        />
      )}
    </div>
  </div>
);

export default MemberSearchBar;
