import React from 'react';
import PropTypes from 'prop-types';

const Header = ({ icon, children }) => (
  <div className="Header">
    <i className={`fa fa-${icon}`} />
    <div className="Header-label">
      {children}
    </div>
  </div>
);

Header.propTypes = {
  children: PropTypes.any.isRequired,
  icon: PropTypes.string.isRequired,
};

export default Header;
