import React from 'react';
import PropTypes from 'prop-types';

import Panel from '../Panel';

const ValueDisplay = ({ label, children, color = '#3d3d3d' }) => (
  <Panel>
    <div style={{
      fontWeight: 100,
      fontSize: 24,
      color,
      overflow: 'hidden'
    }}>
      {children}
    </div>
    <div>
      {label}
    </div>
  </Panel>
);

ValueDisplay.propTypes = {
  label: PropTypes.string.isRequired,
  children: PropTypes.any.isRequired,
};

export default ValueDisplay;