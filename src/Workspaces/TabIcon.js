import React from 'react';
import { Icon } from 'antd';
import PropTypes from 'prop-types';

const TabIcon = ({ icon, name }) => (
  <span>
    <Icon type={icon} />
    {name}
  </span>
);

TabIcon.propTypes = {
  icon: PropTypes.string.isRequired,
  name: PropTypes.string.isRequired,
};

export default TabIcon;
