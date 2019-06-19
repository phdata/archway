import { Icon, Spin, Tooltip } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { getClusterInfo } from '../../Home/selectors';
import { Cluster, Status } from '../../../models/Cluster';

interface Props {
  cluster: Cluster;
}

const ClusterInfo = ({ cluster }: Props) => {
  if (cluster.name === 'Unknown') {
    const [isTimeout, setIsTimeout] = React.useState(false);
    setTimeout(() => {
      setIsTimeout(true);
    }, 30000);

    return (
      <div style={{ textAlign: 'center', color: 'white' }}>
        {isTimeout ? (
          <React.Fragment>Could not connect to Cluster</React.Fragment>
        ) : (
          <Spin
            style={{ color: 'white' }}
            tip="Connecting to Cluster"
            indicator={<Icon type="loading" spin={true} style={{ color: 'white' }} />}
          />
        )}
      </div>
    );
  }

  const status = new Status<Cluster>(cluster);

  return (
    <Tooltip placement="right" title={`${cluster.name}'s status is currently ${status.statusText()}`}>
      <div
        style={{
          padding: 10,
          color: 'white',
          backgroundColor: status.statusColor().string(),
          textAlign: 'center',
        }}
      >
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

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(ClusterInfo);
