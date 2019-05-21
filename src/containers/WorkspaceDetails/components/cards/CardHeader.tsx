import * as React from 'react';

interface Props {
  children: React.ReactNode;
}

const CardHeader = ({ children }: Props) => (
  <div
    style={{
      fontSize: 18,
      marginBottom: 16,
      textTransform: 'uppercase',
      display: 'flex',
      alignItems: 'center',
    }}
  >
    {children}
  </div>
);

export default CardHeader;
