import React from 'react';
import { ConnectedRouter } from 'react-router-redux';
import { Route, Redirect } from 'react-router';
import PropTypes from 'prop-types';
import { Layout } from 'antd';
import './less/index.css';

import Home from './Home';
import Workspaces from './Workspaces';
import WorkspaceDetails from './Workspaces/WorkspaceDetails';
import Request from './Workspaces/Request';
import Navigation from './Navigation';

const { Content, Footer } = Layout;

const Main = ({
  history,
  location
}) => (
  <ConnectedRouter history={history}>
    <Layout style={{ minHeight: '100%' }}>
      <Navigation />
      <Layout style={{ minHeight: '100%', marginLeft: 250, backgroundColor: '#F0F3F5' }}>
        <Content style={{ margin: '24px 16px 0', overflow: 'initial' }}>
          <div style={{
            padding: 24,
            minHeight: '100%',
            background: '#fff',
          }}
          >
            <Route exact path="/" component={() => <Redirect to="/home" />} />
            <Route path="/home" component={Home} />
            <Route exact path="/workspaces" component={Workspaces} />
            <Route path="/request" component={Request} />
            <Route path="/workspaces/:id" component={WorkspaceDetails} />
          </div>
        </Content>
        <Footer style={{ textAlign: 'center' }}>
          Heimdali &copy;2018 Created by Jotunn LLC
        </Footer>
      </Layout>
    </Layout>
  </ConnectedRouter>
);

Main.propTypes = {
  history: PropTypes.object,
};

export default Main;