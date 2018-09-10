import * as React from 'react';
import { ConnectedRouter } from 'react-router-redux';
import { Route, Redirect } from 'react-router';
import { Layout } from 'antd';
import './less/index.css';

import Home from './Home';
import Workspaces from './Workspaces';
import WorkspaceDetails from './Workspaces/WorkspaceDetails';
import Request from './Workspaces/Request';
import Navigation from './Navigation';

const { Content, Footer } = Layout;

const SendHome = () => <Redirect to="/home" />;

const Main = () => (
  <Layout style={{ minHeight: '100%' }}>
    <Navigation />
    <Layout style={{ minHeight: '100%', marginLeft: 250, backgroundColor: '#F0F3F5' }}>
      <Content style={{ margin: '24px 16px 0', overflow: 'initial' }}>
        <div style={{ minHeight: '100%' }}>
          <Route exact={true} path="/" component={SendHome} />
          <Route path="/home" component={Home} />
          <Route exact={true} path="/workspaces" component={Workspaces} />
          <Route path="/request" component={Request} />
          <Route path="/workspaces/:id" component={WorkspaceDetails} />
        </div>
      </Content>
      <Footer style={{ textAlign: 'center' }}>
        Heimdali &copy;2018 Created by Jotunn LLC
      </Footer>
    </Layout>
  </Layout>
);

export default Main;
