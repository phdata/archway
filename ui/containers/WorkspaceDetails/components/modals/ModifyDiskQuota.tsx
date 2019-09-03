import * as React from 'react';
import { Field, reduxForm } from 'redux-form/immutable';
import { InputNumber } from 'antd';

import { QuotaForm } from '../../../../models/Data';
import { HiveAllocation } from '../../../../models/Workspace';

// /* tslint:disable:no-var-requires */
// const { createComponent, customMap } = require('redux-form-antd');

interface Props {
  allocations: HiveAllocation[];
}

const QuotaInput = ({ input: { value, onChange } }: any) => (
  <InputNumber min={1} onChange={onChange} value={value} autoFocus />
);

const ModifyDiskQuota = ({ allocations }: Props) => (
  <div>
    {allocations.map((allocation: HiveAllocation, index: number) => (
      <div style={{ fontSize: 16 }} key={index}>
        <label>Modify disk quota for {allocation.location} </label>
        <Field name="quota" component={QuotaInput} style={{ marginBottom: 0 }} />
        <label>GB</label>
      </div>
    ))}
  </div>
);

export default reduxForm<QuotaForm, Props>({
  form: 'modifyDiskQuotaRequest',
  initialValues: {
    quota: 1,
  },
})(ModifyDiskQuota);
