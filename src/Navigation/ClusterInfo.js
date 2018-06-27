import React from 'react';
import { connect } from 'react-redux';
import { Tag, Spin, Icon } from 'antd';
import './ClusterInfo.css';

const ClusterInfo = ({ name, status = 'unknown' }) => {
  if (name === 'Unknown') { return <Spin indicator={<Icon type="loading" spin style={{ color: 'white' }} />} />; }
  let color = '#F0F3F5';
  let displayStatus = 'unknown';
  switch (status) {
    case 'GOOD_HEALTH':
      color = 'green';
      displayStatus = 'good';
      break;
    case 'BAD_HEALTH':
      color = 'red';
      displayStatus = 'bad';
      break;
  }
  return (
    <div>
      {name} status: &nbsp;
      <Tag color={color} size="medium">{displayStatus}</Tag>
    </div>
  );
};

export default connect(
  state => state.cluster,
  {},
)(ClusterInfo);
