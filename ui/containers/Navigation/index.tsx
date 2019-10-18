import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { Layout, Menu, Icon } from 'antd';
import { NavLink, withRouter, RouteComponentProps, Link } from 'react-router-dom';
import { Profile } from './components';
import * as selectors from '../../redux/selectors';
import { Profile as UserProfile } from '../../models/Profile';
import { ManagePage } from '../Manage/constants';
import { FeatureFlagType } from '../../constants';

function getKeyFromPath(path: string): string {
  if (path.startsWith('/home')) {
    return 'home';
  }
  if (path.startsWith('/workspaces')) {
    return 'workspaces';
  }
  if (path.startsWith('/request')) {
    return 'request';
  }
  if (path.startsWith('/risks')) {
    return 'risks';
  }
  if (path.startsWith('/manage')) {
    return 'manage';
  }
  if (path.startsWith('/operations')) {
    return 'operations';
  }

  return '';
}

interface Props extends RouteComponentProps<any> {
  profile: UserProfile;
  featureFlags: string[];
}

const manageNavigation = (profile: UserProfile, featureFlags: string[]): string => {
  if (profile.permissions.risk_management && featureFlags.includes(FeatureFlagType.ComplianceBuilder)) {
    return ManagePage.ComplianceTab;
  } else if (profile.permissions.platform_operations) {
    return ManagePage.LinksTab;
  } else {
    return ManagePage.ComplianceTab;
  }
};

const Navigation = ({ location, profile, featureFlags }: Props) => (
  <Layout.Sider width={250} style={{ overflow: 'auto', height: '100vh', position: 'fixed', left: 0 }}>
    <Link to="/home">
      <img
        src="images/logo_big_white.png"
        style={{ padding: 25, width: '100%', height: 250, objectFit: 'contain' }}
        alt="Archway Logo"
      />
    </Link>
    <Menu
      style={{
        marginTop: 25,
        letterSpacing: 1,
        textTransform: 'uppercase',
      }}
      selectedKeys={[getKeyFromPath(location.pathname)]}
      theme="dark"
      mode="inline"
    >
      <Menu.Item key="home">
        <NavLink to="/home">
          <Icon type="home" style={{ fontSize: 18 }} /> Overview
        </NavLink>
      </Menu.Item>
      <Menu.Item key="workspaces">
        <NavLink to="/workspaces">
          <Icon type="gift" style={{ fontSize: 18 }} /> Your Workspaces
        </NavLink>
      </Menu.Item>
      {profile && profile.permissions.risk_management && (
        <Menu.Item key="risks">
          <NavLink to="/risks">
            <Icon type="audit" style={{ fontSize: 18 }} /> Risk/Compliance
          </NavLink>
        </Menu.Item>
      )}
      {profile && profile.permissions.platform_operations && (
        <Menu.Item key="operations">
          <NavLink to="/operations">
            <Icon type="cloud" style={{ fontSize: 18 }} /> Platform Operations
          </NavLink>
        </Menu.Item>
      )}
      {profile &&
        (profile.permissions.platform_operations ||
          (profile.permissions.risk_management && featureFlags.includes(FeatureFlagType.ComplianceBuilder))) && (
          <Menu.Item key="manage">
            <NavLink to={`/manage/${manageNavigation(profile, featureFlags)}`}>
              <Icon type="profile" style={{ fontSize: 18 }} /> Manage
            </NavLink>
          </Menu.Item>
        )}
      <Menu.Item key="request">
        <NavLink to="/request">
          <Icon type="plus" style={{ fontSize: 18 }} /> New Workspace
        </NavLink>
      </Menu.Item>
    </Menu>
    <Profile />
  </Layout.Sider>
);

const mapStateToProps = () =>
  createStructuredSelector({
    profile: selectors.getProfile(),
    featureFlags: selectors.getFeatureFlags(),
  });

export default withRouter(connect(mapStateToProps)(Navigation));
