import React from 'react';

const Panel = ({ children, style = {} }) => (
  <div style={{
    ...style,
    textAlign: 'center',
    fontFamily: "Roboto",
    backgroundColor: '#ffffff',
    padding: 10,
    margin: 10,
    borderRadius: 10,
    boxShadow: '0 0 5px #3d3d3d',
    flex: 1,
  }}>
    {children}
  </div>
);

export default Panel;