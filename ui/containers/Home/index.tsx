import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { Spin, Icon } from 'antd';

import * as actions from '../Login/actions';
import { refreshRecentWorkspaces } from './actions';
import { Workspace } from '../../models/Workspace';
import {
  Cluster,
  HiveService,
  HiveServiceLinks,
  HueService,
  HueServiceLinks,
  Status,
  YarnService,
  YarnServiceLinks,
  NavigatorService,
  NavigatorServiceLinks,
} from '../../models/Cluster';

import { PersonalWorkspace, RecentWorkspaces, Service as ServiceDisplay } from './components';
import {
  getClusterInfo,
  getPersonalWorkspace,
  getRecentWorkspaces,
  isClusterLoading,
  isProfileLoading,
} from './selectors';

/* tslint:disable:no-var-requires */
const router = require('connected-react-router/immutable');

interface States {
  clusterTimeout: boolean;
}

interface Props {
  cluster: Cluster;
  clusterLoading: boolean;
  personalWorkspace: Workspace;
  recentWorkspaces: Workspace[];
  profileLoading: boolean;
  refreshRecentWorkspaces: () => void;
  requestWorkspace: () => void;
  openWorkspace: (id: number) => void;
}

class Home extends React.Component<Props, States> {
  public state = {
    clusterTimeout: false,
  };

  public componentDidMount() {
    this.props.refreshRecentWorkspaces();
  }

  public renderClusterStatus = () => {
    const { cluster } = this.props;
    const clusterStatus = new Status<Cluster>(cluster);

    return cluster.name === 'Unknown' ? (
      <Spin
        style={{ color: 'black', fontSize: '30px', fontWeight: 200 }}
        tip="Connecting to Cluster"
        indicator={<Icon type="loading" spin={true} style={{ color: 'black', fontSize: '30px' }} />}
      />
    ) : (
      <React.Fragment>
        <h1 style={{ fontWeight: 100 }}>You are currently connected to {cluster.name}!</h1>
        <h3 style={{ fontWeight: 100 }}>
          The current status of {cluster.name} is{' '}
          <span
            style={{
              fontWeight: 'bold',
              color: clusterStatus.statusColor().string(),
            }}
          >
            {clusterStatus.statusText()}
          </span>
        </h3>
        <h2>
          <a target="_blank" rel="noreferrer noopener" href={cluster.cm_url}>
            {cluster.name}&apos;s Cloudera Manager UI
          </a>
        </h2>
      </React.Fragment>
    );
  };

  public render() {
    const {
      cluster,
      clusterLoading,
      personalWorkspace,
      recentWorkspaces,
      profileLoading,
      requestWorkspace,
      openWorkspace,
    } = this.props;
    const { clusterTimeout } = this.state;

    if (!cluster) {
      return <div />;
    } else if (cluster.name === 'Unknown' && !clusterLoading) {
      setTimeout(() => {
        this.setState({ clusterTimeout: true });
      }, 30000);
    }

    return (
      <div>
        <div
          style={{ padding: 24, background: '#fff', textAlign: 'center', height: clusterTimeout ? '138px' : '100%' }}
        >
          {clusterTimeout || this.renderClusterStatus()}
        </div>
        <div style={{ display: 'flex', padding: '25px 12px' }}>
          <ServiceDisplay
            name="Hive"
            status={new Status<HiveService>(cluster.services.hive)}
            links={new HiveServiceLinks(cluster.services.hive)}
            index={0}
          />
          <ServiceDisplay
            name="Hue"
            status={new Status<HueService>(cluster.services.hue)}
            links={new HueServiceLinks(cluster.services.hue)}
            index={1}
          />
          <ServiceDisplay
            name="Yarn"
            status={new Status<YarnService>(cluster.services.yarn)}
            links={new YarnServiceLinks(cluster.services.yarn)}
            index={2}
          />
          <ServiceDisplay
            name="Navigator"
            status={new Status<NavigatorService>(cluster.services.mgmt)}
            links={new NavigatorServiceLinks(cluster.services.mgmt)}
            index={3}
          />
        </div>
        <PersonalWorkspace
          loading={profileLoading}
          requestWorkspace={requestWorkspace}
          workspace={personalWorkspace}
          services={cluster.services}
        />
        <RecentWorkspaces workspaces={recentWorkspaces} onSelectWorkspace={openWorkspace} />
      </div>
    );
  }
}

const mapStateToProps = () =>
  createStructuredSelector({
    cluster: getClusterInfo(),
    clusterLoading: isClusterLoading(),
    personalWorkspace: getPersonalWorkspace(),
    profileLoading: isProfileLoading(),
    recentWorkspaces: getRecentWorkspaces(),
  });

const mapDispatchToProps = (dispatch: any) => ({
  refreshRecentWorkspaces: () => dispatch(refreshRecentWorkspaces()),
  requestWorkspace: () => dispatch(actions.requestWorkspace()),
  openWorkspace: (id: number) => dispatch(router.push(`/workspaces/${id}`)),
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Home);
