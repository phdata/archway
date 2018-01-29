import React from 'react';
import './Panel.css';

const Panel = ({title, children}) => {

    return (
      <div className="Panel">
        <h2 className="Panel-title">
          {title}
        </h2>
        <div className="Panel-content">
          {children}
        </div>
      </div>
    );

};

export default Panel;