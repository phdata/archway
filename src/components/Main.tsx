import { Layout } from 'antd';
import * as React from 'react';
import { Redirect, Route, Switch } from 'react-router';
import Navigation from '../containers/Navigation';
import Home from '../containers/Home';
import WorkspaceDetails from '../containers/WorkspaceDetails';
import WorkspaceListing from '../containers/WorkspaceListing';
import WorkspaceRequest from '../containers/WorkspaceRequest';

const { Content, Footer } = Layout;

const SendHome = () => <Redirect to="/home" />;

const Main = () => (
  <Layout style={{ minHeight: '100%' }}>
    <Navigation />
    <Layout style={{ minHeight: '100%', marginLeft: 250, backgroundColor: '#F0F3F5' }}>
      <Content style={{ margin: '24px 16px 0', overflow: 'initial' }}>
        <div style={{ minHeight: '100%' }}>
          <Switch>
            <Route exact={true} path="/" component={SendHome} />
            <Route path="/home" component={Home} />
            <Route exact={true} path="/workspaces" component={WorkspaceListing} />
            <Route path="/request" component={WorkspaceRequest} />
            <Route path="/workspaces/:id" component={WorkspaceDetails} />
          </Switch>
        </div>
      </Content>
      <Footer style={{ textAlign: 'center' }}>
        Heimdali &copy;2018 Created by Jotunn LLC
      </Footer>
    </Layout>
  </Layout>
);

export default Main;
