import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { Workspace } from '../../types/Workspace';
import * as actions from '../Login/actions';
import {
  Cluster,
  HiveService,
  HiveServiceLinks,
  HueService,
  HueServiceLinks,
  Status,
  YarnService,
  YarnServiceLinks,
} from '../../types/Cluster';
import PersonalWorkspace from './PersonalWorkspace';
import { getClusterInfo, getPersonalWorkspace, isProfileLoading } from './selectors';
import ServiceDisplay from './Service';

interface Props {
  cluster: Cluster;
  personalWorkspace: Workspace;
  profileLoading: boolean;
  requestWorkspace: () => void;
}

const Home = ({ cluster, personalWorkspace, profileLoading, requestWorkspace }: Props) => {

  if (!cluster) { return <div />; }

  const clusterStatus = new Status<Cluster>(cluster);

  return (
    <div>
      <div style={{ padding: 24, background: '#fff', textAlign: 'center', height: '100%' }}>
        <h1 style={{ fontWeight: 100 }}>
          You are currently connected to {cluster.name}!
        </h1>
        <h3 style={{ fontWeight: 100 }}>
          The current status of {cluster.name} is{' '}
          <span
            style={{
                fontWeight: 'bold',
                color: clusterStatus.statusColor().string(),
              }}>{clusterStatus.statusText()}</span>
        </h3>
        <h2>
          <a target="_blank" rel="noreferrer noopener" href={cluster.cm_url}>
            {cluster.name}&apos;s Cloudera Manager UI
          </a>
        </h2>
      </div>
      <div style={{ display: 'flex', marginTop: 25, marginBottom: 25 }}>
        <ServiceDisplay
          name="Hive"
          status={new Status<HiveService>(cluster.services.hive)}
          links={new HiveServiceLinks(cluster.services.hue)}
          index={0} />
        <ServiceDisplay
          name="Hue"
          status={new Status<HueService>(cluster.services.hue)}
          links={new HueServiceLinks(cluster.services.hue)}
          index={1} />
        <ServiceDisplay
          name="Yarn"
          status={new Status<YarnService>(cluster.services.yarn)}
          links={new YarnServiceLinks(cluster.services.yarn)}
          index={2} />
      </div>
      <div style={{ marginTop: 25 }}>
        <PersonalWorkspace
          loading={profileLoading}
          requestWorkspace={requestWorkspace}
          workspace={personalWorkspace}
          services={cluster.services} />
      </div>
    </div>
  );
};

const mapStateToProps = () =>
  createStructuredSelector({
    cluster: getClusterInfo(),
    personalWorkspace: getPersonalWorkspace(),
    profileLoading: isProfileLoading(),
  });

const mapDispatchToProps = (dispatch: any) => ({
  requestWorkspace: () => dispatch(actions.requestWorkspace()),
});

export default connect(mapStateToProps, mapDispatchToProps)(Home);
