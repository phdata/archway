import React from 'react';
import './Panel.css';

const Panel = ({children}) => {

    return (
      <div className="Panel">
          {children}
      </div>
    );

};

export default Panel;