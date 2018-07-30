import React from 'react';
import { Row, Col } from 'antd';
import { Route } from 'react-router';

import WorkspaceList from './WorkspaceList';
import WorkspaceDetails from './WorkspaceDetails';

const Workspaces = ({ match }) => (
  <div>
    <Route exact path={match.path} component={WorkspaceList} />
    <Route path={`${match.path}/:id`} component={WorkspaceDetails} />
  </div>
);

export default Workspaces;
