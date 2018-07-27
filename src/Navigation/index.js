import React from 'react';
import { Layout, Menu, Icon } from 'antd';
import { withRouter, NavLink } from 'react-router-dom';
import { connect } from 'react-redux';

import logo from '../Common/white_logo_transparent.png';
import Profile from './Profile';
import ClusterInfo from './ClusterInfo';
import './Navigation.css';

const { Sider } = Layout;

const Navigation = ({ location }) => (
  <Sider width={250} style={{ overflow: 'auto', height: '100vh', position: 'fixed', left: 0 }}>
    <img src={logo} style={{ padding: 25, width: '100%' }} />
    <Profile />
    <Menu style={{ marginTop: 25 }} selectedKeys={[location.pathname]} theme="dark" mode="inline">
      <Menu.Item key="/home">
        <Icon type="home" />
        <NavLink style={{ display: 'inline-block' }} to="/">
          Overview
        </NavLink>
      </Menu.Item>
      <Menu.Item key="/workspaces">
        <Icon type="api" />
        <NavLink style={{ display: 'inline-block' }} to="/workspaces">Workspaces</NavLink>
      </Menu.Item>
      <Menu.Item key="/request">
        <Icon type="plus" />
        <NavLink style={{ display: 'inline-block' }} to="/request">New Workspace</NavLink>
      </Menu.Item>
    </Menu>
    <ClusterInfo />
  </Sider>
);

export default withRouter(Navigation);
