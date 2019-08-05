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
import CustomWorkspaces from '../containers/CustomWorkspaces';

const { Content, Footer } = Layout;

interface Props {
  version: string;
}

const Main = ({ version }: Props) => (
  <Layout style={{ minHeight: '100%' }}>
    <Navigation />
    <Layout style={{ minHeight: '100%', marginLeft: 250, backgroundColor: '#F0F3F5' }}>
      <Content style={{ overflow: 'initial' }}>
        <div style={{ minHeight: '100%' }}>
          <Switch>
            <Route path="/home" component={Home} />
            <Route exact path="/workspaces" component={WorkspaceListing} />
            <Route exact path="/risks" component={RiskListing} />
            <Route exact path="/operations" component={OpsListing} />
            <Route exact path="/request" component={WorkspaceRequest} />
            <Route exact path="/request/customworkspaces" component={CustomWorkspaces} />
            <Route path="/workspaces/:id" component={WorkspaceDetails} />
            <Redirect to="/home" />
          </Switch>
        </div>
      </Content>

      <Footer style={{ textAlign: 'center' }}>
        Archway &copy; {new Date().getFullYear()} Created by phData
        <br />
        {!!version && `Version ${version}`}
      </Footer>
    </Layout>
  </Layout>
);

export default Main;
