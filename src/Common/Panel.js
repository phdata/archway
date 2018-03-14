import React from 'react';
import './Panel.css';

const Panel = (params) => {
    const {className, children} = params;
    return (
      <div {...params} className={`Panel ${className}`}>
          {children}
      </div>
    );

};

export default Panel;