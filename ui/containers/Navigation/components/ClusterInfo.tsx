import { Icon, Spin, Tooltip } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { getClusterInfo, isClusterLoading } from '../../Home/selectors';
import { Cluster, Status } from '../../../models/Cluster';
import { Colors } from '../../../components';

interface Props {
  cluster: Cluster;
  loading: boolean;
}

const ClusterInfo = ({ cluster, loading }: Props) => {
  const errored = cluster.name === 'Unknown';

  if (errored || loading) {
    const [isTimeout, setIsTimeout] = React.useState(false);
    if (!loading && errored) {
      setTimeout(() => {
        setIsTimeout(true);
      }, 30000);
    }
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
          backgroundColor: Colors.Gray.toString(),
          textAlign: 'center',
        }}
      >
        <a
          target="_blank"
          rel="noreferrer noopener"
          href={cluster.cm_url}
          style={{ color: Colors.PrimaryColor.string() }}
        >
          {cluster.name} status : <span style={{ color: status.statusColor().toString() }}>{status.statusText()}</span>
        </a>
      </div>
    </Tooltip>
  );
};

const mapStateToProps = () =>
  createStructuredSelector({
    cluster: getClusterInfo(),
    loading: isClusterLoading(),
  });

const mapDispatchToProps = {};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(ClusterInfo);
