import * as React from 'react';

interface Props {
  children: React.ReactNode;
}

const FieldLabel = ({ children }: Props) => (
  <div style={{ color: 'rgba(0, 0, 0, .5)', textAlign: 'center', textTransform: 'uppercase', fontSize: 10 }}>
    {children}
  </div>
);

export default FieldLabel;
