import { Icon, Spin, Tooltip } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { Cluster, Status } from '../types/Cluster';
import { getClusterInfo } from '../Home/selectors';

const anchor = {
  width: '100%',
  position: 'absolute' as 'absolute', // https://github.com/Microsoft/TypeScript/issues/11465
  bottom: 0,
}

interface Props {
  cluster: Cluster
}

const ClusterInfo = ({ cluster }: Props) => {
  if (cluster.name === 'Unknown')
    return (
      <Spin
        style={{ color: 'white', ...anchor}}
        tip="fetching cluster"
        indicator={<Icon type="loading" spin={true} style={{ color: 'white' }} />} />
    );

  const status = new Status<Cluster>(cluster);

  return (
    <Tooltip placement="right" title={`${cluster.name}'s status is currently ${status.statusText()}`}>
      <div style={{ padding: 10, color: 'white', backgroundColor: status.statusColor().string(), textAlign: 'center', ...anchor }}>
        {cluster.name} is {status.statusText()}
      </div>
    </Tooltip>
  );
};

const mapStateToProps = () =>
  createStructuredSelector({
    cluster: getClusterInfo(),
  });

const mapDispatchToProps = {};

export default connect(mapStateToProps, mapDispatchToProps)(ClusterInfo);