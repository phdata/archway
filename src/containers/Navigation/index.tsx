import { Layout, Menu, Icon } from 'antd';
import * as React from 'react';
import { NavLink, withRouter } from 'react-router-dom';
import { ClusterInfo, Profile } from './components';

const Navigation = ({ location }: any) => (
  <Layout.Sider width={250} style={{ overflow: 'auto', height: '100vh', position: 'fixed', left: 0 }}>
    <img src="images/white_logo_transparent.png" style={{ padding: 25, width: '100%' }} alt="Heimdali Logo" />
    <ClusterInfo />
    <Menu style={{
      marginTop: 25,
      letterSpacing: 1,
      textTransform: 'uppercase',
    }} selectedKeys={[location.pathname]} theme="dark" mode="inline">
      <Menu.Item style={{ marginTop: 20 }} key="/home">
        <NavLink to="/">
          <Icon type="home" style={{ fontSize: 18 }} /> Overview
        </NavLink>
      </Menu.Item>
      <Menu.Item style={{ marginTop: 20 }} key="/workspaces">
        <NavLink to="/workspaces">
        <Icon type="gift" style={{ fontSize: 18 }} /> Your Workspaces
        </NavLink>
      </Menu.Item>
      <Menu.Item style={{ marginTop: 20 }} key="/request">
        <NavLink to="/request">
        <Icon type="plus" style={{ fontSize: 18 }} /> New Workspace
        </NavLink>
      </Menu.Item>
    </Menu>
    <Profile />
  </Layout.Sider>
);

export default withRouter(Navigation);
