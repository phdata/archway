import React from 'react';
import { ConnectedRouter } from 'react-router-redux';
import { Route, Redirect } from 'react-router';
import PropTypes from 'prop-types';
import { Layout, Menu } from 'antd';
import { NavLink } from 'react-router-dom';
import './less/index.css';

import logo from './Navigation/logo.png';
import Workspaces from './Workspaces';
import WorkspaceDetails from './Workspaces/WorkspaceDetails';
import Request from './Workspaces/Request';
import ClusterInfo from './Navigation/ClusterInfo';

const { Header, Content, Footer } = Layout;

const Main = ({
  history,
}) => (
  <ConnectedRouter history={history}>
    <Layout style={{ minHeight: '100%' }}>
      <Header style={{ height: 71 }}>
        <span style={{ float: 'left', maxHeight: 50, margin: '5px 24px 10px 0' }}>
          <img src={logo} alt="logo" style={{ maxHeight: 50, marginTop: -15, marginRight: 15 }} />
          <h1 style={{ color: 'white', display: 'inline', fontFamily: 'Playfair Display' }}>Heimdali</h1>
        </span>
        <span style={{ float: 'right', color: 'white' }}>
          <ClusterInfo />
        </span>
        <Menu defaultSelectedKeys={['workspaces']} theme="dark" mode="horizontal" style={{ fontWeight: 100, lineHeight: '71px' }}>
          <Menu.Item style={{ fontSize: 18, letterSpacing: 1 }} key="workspaces">
            <NavLink to="/workspaces">
              WORKSPACES
            </NavLink>
          </Menu.Item>
        </Menu>
      </Header>
      <Content style={{
        padding: '0 50px',
        marginTop: 64,
        width: 1140,
        marginLeft: 'auto',
        marginRight: 'auto',
      }}
      >
        <div style={{
          padding: 24,
          minHeight: 200,
          background: '#fff',
        }}
        >
          <Route exact path="/" component={() => <Redirect to="/workspaces" />} />
          <Route exact path="/workspaces" component={Workspaces} />
          <Route path="/request" component={Request} />
          <Route path="/workspaces/:id" component={WorkspaceDetails} />
        </div>
      </Content>
      <Footer style={{ textAlign: 'center' }}>
        Heimdali &copy;2018 Created by Jotunn LLC
      </Footer>
    </Layout>
  </ConnectedRouter>
);

Main.propTypes = {
  history: PropTypes.object,
};

export default Main;
