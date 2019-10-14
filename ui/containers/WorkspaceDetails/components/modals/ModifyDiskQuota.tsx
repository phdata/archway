import * as React from 'react';
import { Field, reduxForm } from 'redux-form/immutable';
import { InputNumber } from 'antd';

import { QuotaForm } from '../../../../models/Form';
import { HiveAllocation } from '../../../../models/Workspace';

// /* tslint:disable:no-var-requires */
// const { createComponent, customMap } = require('redux-form-antd');

interface Props {
  allocation: HiveAllocation;
}

const QuotaInput = ({ input: { value, onChange } }: any) => (
  <InputNumber min={1} onChange={onChange} value={value} autoFocus />
);

const ModifyDiskQuota = ({ allocation }: Props) => (
  <div style={{ fontSize: 16 }}>
    <label>Modify disk quota for {allocation.location} </label>
    <Field name="quota" component={QuotaInput} style={{ marginBottom: 0 }} />
    <label>GB</label>
  </div>
);

export default reduxForm<QuotaForm, Props>({
  form: 'modifyDiskQuotaRequest',
  initialValues: {
    quota: 1,
  },
})(ModifyDiskQuota);
