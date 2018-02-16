import React from 'react';
import './Panel.css';

const Panel = ({className, children}) => {

    return (
      <div className={`Panel ${className}`}>
          {children}
      </div>
    );

};

export default Panel;