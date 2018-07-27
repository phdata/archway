import React from 'react';
import PropTypes from 'prop-types';

import './ValueDisplay.css';

const ValueDisplay = ({ label, children, color = '#3d3d3d' }) => (
  <div className="ValueDisplay">
    <div className="Value" style={{ color, overflow: 'hidden' }}>
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
