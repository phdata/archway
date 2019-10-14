import * as React from 'react';
import { Field, reduxForm } from 'redux-form/immutable';
import { InputNumber } from 'antd';

import { CoreMemoryForm } from '../../../../models/Form';

interface Props {
  poolName: string;
}

const CoreInput = ({ input: { value, onChange } }: any) => (
  <InputNumber min={1} onChange={onChange} value={value} autoFocus />
);

const MemoryInput = ({ input: { value, onChange } }: any) => (
  <InputNumber min={1} onChange={onChange} value={value} autoFocus />
);

const ModifyCoreMemory = ({ poolName }: Props) => (
  <div style={{ fontSize: 16 }}>
    <label>Modify {poolName} </label>
    <br />
    <br />
    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
      <label>Cores:</label>
      <Field name="core" component={CoreInput} style={{ marginBottom: 0 }} />
    </div>
    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
      <label>Memory (GB):</label>
      <Field name="memory" component={MemoryInput} style={{ marginBottom: 0 }} />
    </div>
  </div>
);

export default reduxForm<CoreMemoryForm, Props>({
  form: 'modifyCoreMemoryRequest',
  initialValues: {
    core: 1,
    memory: 1,
  },
})(ModifyCoreMemory);
