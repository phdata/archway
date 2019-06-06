import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
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
  NavigatorServiceLinks
} from '../../models/Cluster';

import { PersonalWorkspace, RecentWorkspaces, Service as ServiceDisplay } from './components';
import { getClusterInfo, getPersonalWorkspace, getRecentWorkspaces, isProfileLoading } from './selectors';

/* tslint:disable:no-var-requires */
const router = require('connected-react-router/immutable');

interface Props {
  cluster: Cluster;
  personalWorkspace: Workspace;
  recentWorkspaces: Workspace[];
  profileLoading: boolean;
  refreshRecentWorkspaces: () => void;
  requestWorkspace: () => void;
  openWorkspace: (id: number) => void;
}

class Home extends React.Component<Props> {
  public componentDidMount() {
    this.props.refreshRecentWorkspaces();
  }

  public render() {
    const {
      cluster,
      personalWorkspace,
      recentWorkspaces,
      profileLoading,
      requestWorkspace,
      openWorkspace
    } = this.props;

    if (!cluster) {
      return <div />;
    }

    const clusterStatus = new Status<Cluster>(cluster);

    return (
      <div>
        <div style={{ padding: 24, background: '#fff', textAlign: 'center', height: '100%' }}>
          <h1 style={{ fontWeight: 100 }}>You are currently connected to {cluster.name}!</h1>
          <h3 style={{ fontWeight: 100 }}>
            The current status of {cluster.name} is{' '}
            <span
              style={{
                fontWeight: 'bold',
                color: clusterStatus.statusColor().string()
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
    personalWorkspace: getPersonalWorkspace(),
    profileLoading: isProfileLoading(),
    recentWorkspaces: getRecentWorkspaces()
  });

const mapDispatchToProps = (dispatch: any) => ({
  refreshRecentWorkspaces: () => dispatch(refreshRecentWorkspaces()),
  requestWorkspace: () => dispatch(actions.requestWorkspace()),
  openWorkspace: (id: number) => dispatch(router.push(`/workspaces/${id}`))
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Home);
