import * as React from 'react';

interface Props {
  style?: React.CSSProperties;
  children: any;
}

const Label = ({ style, children }: Props) => (
  <div
    style={{
        marginBottom: 10,
        fontSize: 16,
        textTransform: 'uppercase',
        letterSpacing: 1,
        fontWeight: 200,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        ...style,
      }}>
    {children}
  </div>
);

export default Label;