import React from 'react';
import { connect } from 'react-redux';
import { Spin, Avatar, Icon, Menu, Tooltip } from 'antd';
import { NavLink, withRouter } from 'react-router-dom';
import './ClusterInfo.css';

const ClusterInfo = ({ name, displayStatus, color, location }) => {
  if (name === 'Unknown') { return <Spin indicator={<Icon type="loading" spin style={{ color: 'white' }} />} />; }
  return (
    <Tooltip placement="right" title={`${name}'s status is currently ${displayStatus}`}>
      <div style={{ padding: 10, color: 'white', backgroundColor: color, textAlign: 'center' }}>
        {name} is {displayStatus}
      </div>
    </Tooltip>
  );
};

export default connect(
  state => state.cluster,
  {},
)(withRouter(ClusterInfo));
