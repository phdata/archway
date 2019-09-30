import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { Spin, Icon, List } from 'antd';

import * as actions from '../Login/actions';
import { refreshRecentWorkspaces } from './actions';
import { getLinksGroups } from '../Manage/actions';

import { Workspace } from '../../models/Workspace';
import { Cluster } from '../../models/Cluster';

import { PersonalWorkspace, RecentWorkspaces } from './components';
import {
  getClusterInfo,
  getPersonalWorkspace,
  getRecentWorkspaces,
  getProvisioning,
  isClusterLoading,
  isProfileLoading,
  isWorkspaceFetched,
} from './selectors';
import * as manageSelectors from '../Manage/selectors';
import { ProvisioningType, LinksGroupCardPage } from '../../constants';
import { LinksGroup } from '../../models/Manage';
import { LinksGroupCard } from '../Manage/components';

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
  provisioning: ProvisioningType;
  workspaceFetched: boolean;
  isLinksGroupsLoading: boolean;
  linksGroups: LinksGroup[];

  refreshRecentWorkspaces: () => void;
  requestWorkspace: () => void;
  openWorkspace: (id: number) => void;
  fetchLinksGroups: () => void;
}

class Home extends React.Component<Props, States> {
  public state = {
    clusterTimeout: false,
  };

  public componentDidMount() {
    this.props.refreshRecentWorkspaces();
    this.props.fetchLinksGroups();
  }

  public renderClusterStatus = () => {
    const { cluster } = this.props;

    return cluster.name === 'Unknown' ? (
      <Spin
        style={{ color: 'black', fontSize: '30px', fontWeight: 200 }}
        tip="Connecting to Cluster"
        indicator={<Icon type="loading" spin={true} style={{ color: 'black', fontSize: '30px' }} />}
      />
    ) : (
      <React.Fragment>
        <h1 style={{ fontWeight: 100 }}>You are currently connected to {cluster.name}!</h1>
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
      provisioning,
      personalWorkspace,
      recentWorkspaces,
      profileLoading,
      workspaceFetched,
      requestWorkspace,
      openWorkspace,
      linksGroups,
      isLinksGroupsLoading,
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
        <div style={{ padding: '25px 12px', textAlign: 'center' }}>
          <h2>Links</h2>
          <List
            grid={{ gutter: 32, lg: 3, md: 2, sm: 1 }}
            dataSource={linksGroups}
            loading={isLinksGroupsLoading}
            renderItem={(linksGroup: LinksGroup) => (
              <List.Item>
                <LinksGroupCard linksGroup={linksGroup} page={LinksGroupCardPage.Overview} />
              </List.Item>
            )}
          />
        </div>
        <PersonalWorkspace
          loading={profileLoading}
          requestWorkspace={requestWorkspace}
          workspace={personalWorkspace}
          services={cluster.services}
          provisioning={provisioning}
          workspaceFetched={workspaceFetched}
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
    provisioning: getProvisioning(),
    workspaceFetched: isWorkspaceFetched(),
    linksGroupsLoading: manageSelectors.isLoading(),
    linksGroups: manageSelectors.getLinksGroups(),
  });

const mapDispatchToProps = (dispatch: any) => ({
  refreshRecentWorkspaces: () => dispatch(refreshRecentWorkspaces()),
  requestWorkspace: () => dispatch(actions.requestWorkspace()),
  openWorkspace: (id: number) => dispatch(router.push(`/workspaces/${id}`)),
  fetchLinksGroups: () => dispatch(getLinksGroups()),
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Home);
