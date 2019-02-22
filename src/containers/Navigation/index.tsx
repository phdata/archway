import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { Layout, Menu, Icon } from 'antd';
import { NavLink, withRouter, RouteComponentProps } from 'react-router-dom';
import { ClusterInfo, Profile } from './components';
import * as selectors from '../../redux/selectors';
import { Profile as UserProfile } from '../../models/Profile';

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
  if (path.startsWith('/operations')) {
    return 'operations';
  }

  return '';
}

interface Props extends RouteComponentProps<any> {
  profile: UserProfile;
}

const Navigation = ({ location, profile }: Props) => (
  <Layout.Sider width={250} style={{ overflow: 'auto', height: '100vh', position: 'fixed', left: 0 }}>
    <img src="images/white_logo_transparent.png" style={{ padding: 25, width: '100%' }} alt="Heimdali Logo" />
    <ClusterInfo />
    <Menu style={{
      marginTop: 25,
      letterSpacing: 1,
      textTransform: 'uppercase',
    }} selectedKeys={[getKeyFromPath(location.pathname)]} theme="dark" mode="inline">
      <Menu.Item key="home">
        <NavLink to="/">
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
  });

export default withRouter(connect(mapStateToProps)(Navigation));
