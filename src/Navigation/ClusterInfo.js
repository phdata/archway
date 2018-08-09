import React from 'react';
import { connect } from 'react-redux';
import { Spin, Icon, Tooltip } from 'antd';
import { withRouter } from 'react-router-dom';
import './ClusterInfo.css';

const anchor = {
  width: '100%',
  position: 'absolute',
  bottom: 0,
}

const ClusterInfo = ({ name, displayStatus, color, location }) => {
  if (name === 'Unknown') { return <Spin style={{ color: 'white', ...anchor }} tip="fetching cluster" indicator={<Icon type="loading" spin style={{ color: 'white' }} /> }
  />; }
  return (
    <Tooltip placement="right" title={`${name}'s status is currently ${displayStatus}`}>
      <div style={{ padding: 10, color: 'white', backgroundColor: color, textAlign: 'center', ...anchor }}>
        {name} is {displayStatus}
      </div>
    </Tooltip>
  );
};

export default connect(
  state => state.cluster, {},
)(withRouter(ClusterInfo));