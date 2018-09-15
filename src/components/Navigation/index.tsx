import { Layout, Menu } from 'antd';
import * as React from 'react';
import { NavLink, withRouter } from 'react-router-dom';
import logo from '../../components/white_logo_transparent.png';
import ClusterInfo from './ClusterInfo';
import Profile from './Profile';

const { Sider } = Layout;

const Navigation = ({ location }: any) => (
  <Sider width={250} style={{ overflow: 'auto', height: '100vh', position: 'fixed', left: 0 }}>
    <img src={logo} style={{ padding: 25, width: '100%' }} alt="Heimdali Logo" />
    <Profile />
    <Menu style={{ marginTop: 25, letterSpacing: 1, textTransform: 'uppercase' }} selectedKeys={[location.pathname]} theme="dark" mode="inline">
      <Menu.Item style={{ marginTop: 20 }} key="/home">
        <NavLink to="/">
          <i className="fa fa-home" /> Overview
        </NavLink>
      </Menu.Item>
      <Menu.Item style={{ marginTop: 20 }} key="/workspaces">
        <NavLink to="/workspaces">
          <i className="fa fa-flask" /> Your Workspace
        </NavLink>
      </Menu.Item>
      <Menu.Item style={{ marginTop: 20 }} key="/request">
        <NavLink to="/request">
          <i className="fa fa-plus" /> New Workspace
        </NavLink>
      </Menu.Item>
    </Menu>
    <ClusterInfo />
  </Sider>
);

export default withRouter(Navigation);
