import { Layout } from 'antd';
import * as React from 'react';
import { Redirect, Route, Switch } from 'react-router';
import Navigation from '../containers/Navigation';
import Home from '../containers/Home';
import WorkspaceDetails from '../containers/WorkspaceDetails';
import WorkspaceListing from '../containers/WorkspaceListing';
import RiskListing from '../containers/RiskListing';
import OpsListing from '../containers/OpsListing';
import WorkspaceRequest from '../containers/WorkspaceRequest';

const { Content, Footer } = Layout;

const SendHome = () => <Redirect to="/home" />;

const Main = () => (
  <Layout style={{ minHeight: '100%' }}>
    <Navigation />
    <Layout style={{ minHeight: '100%', marginLeft: 250, backgroundColor: '#F0F3F5' }}>
      <Content style={{ overflow: 'initial' }}>
        <div style={{ minHeight: '100%' }}>
          <Switch>
            <Route exact={true} path="/" component={SendHome} />
            <Route path="/home" component={Home} />
            <Route exact={true} path="/workspaces" component={WorkspaceListing} />
            <Route exact={true} path="/risks" component={RiskListing} />
            <Route exact={true} path="/operations" component={OpsListing} />
            <Route path="/request" component={WorkspaceRequest} />
            <Route path="/workspaces/:id" component={WorkspaceDetails} />
          </Switch>
        </div>
      </Content>
      <Footer style={{ textAlign: 'center' }}>
        Heimdali &copy;2018 Created by phData
      </Footer>
    </Layout>
  </Layout>
);

export default Main;
