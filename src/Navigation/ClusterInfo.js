import React from 'react';
import { connect } from 'react-redux';
import { Spin, Icon } from 'antd';
import './ClusterInfo.css';

const ClusterInfo = ({ name, status = 'unknown' }) => {
  if (name === 'Unknown') { return <Spin indicator={<Icon type="loading" spin style={{ color: 'white' }} />} />; }
  let color = '#F0F3F5';
  let displayStatus = 'unknown';
  switch (status) {
    case 'GOOD_HEALTH':
      color = '#43AA8B';
      displayStatus = 'good';
      break;
    case 'CONCERNING_HEALTH':
      color = '#FF6F59';
      displayStatus = 'concerning';
      break;
    case 'BAD_HEALTH':
      color = '#DB504A';
      displayStatus = 'bad';
      break;
    default:
      color = '#aaa';
      displayStatus = 'unknown';
      break;
  }
  return (
    <div style={{ display: 'flex', alignItems: 'center', height: 70 }}>
       <div>{name} status:</div>
      <div style={{ 
               marginLeft: 10, 
               lineHeight: 'normal', 
               borderRadius: 5, 
               backgroundColor: color, 
               fontSize: 12, 
               color: "white",
               padding: 5,
               boxShadow: "0 0 5px white",
           }}>
          {displayStatus}
      </div>
    </div>
  );
};

export default connect(
  state => state.cluster,
  {},
)(ClusterInfo);
