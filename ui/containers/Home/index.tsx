import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { List } from 'antd';

import * as actions from '../Login/actions';
import { refreshRecentWorkspaces } from './actions';
import { getLinksGroups } from '../Manage/actions';

import { Workspace } from '../../models/Workspace';

import { PersonalWorkspace, RecentWorkspaces } from './components';
import {
  getPersonalWorkspace,
  getRecentWorkspaces,
  getProvisioning,
  isProfileLoading,
  isWorkspaceFetched,
} from './selectors';
import * as manageSelectors from '../Manage/selectors';
import { ProvisioningType, LinksGroupCardPage } from '../../constants';
import { LinksGroup } from '../../models/Manage';
import { LinksGroupCard } from '../Manage/components';

/* tslint:disable:no-var-requires */
const router = require('connected-react-router/immutable');

interface Props {
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

class Home extends React.Component<Props> {
  public componentDidMount() {
    this.props.refreshRecentWorkspaces();
    this.props.fetchLinksGroups();
  }

  public render() {
    const {
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

    return (
      <div>
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
