import React from 'react';
import PropTypes from 'prop-types';

import './ValueDisplay.css';

const ValueDisplay = ({ label, children }) => (
  <div className="ValueDisplay">
    <div className="Value">
      {children}
    </div>
    <div className="Label">
      {label}
    </div>
  </div>
);

ValueDisplay.propTypes = {
  label: PropTypes.string.isRequired,
  children: PropTypes.any.isRequired,
};

export default ValueDisplay;
