import React from 'react';
import { push } from 'react-router-redux';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Row, Col } from 'antd';

import { changeActiveWorkspace } from './actions.js';
import WorkspaceList from './WorkspaceList';
import WorkspaceDetails from './WorkspaceDetails';
import './Workspaces.css';

const Workspaces = ({ activeWorkspace, changeActiveWorkspace }) => (
  <Row gutter={16}>
    <Col>
      <WorkspaceList onSelected={changeActiveWorkspace} activeWorkspace={activeWorkspace} />
    </Col>
  </Row>
);

Workspaces.propTypes = {
  activeWorkspace: PropTypes.object,
  changeActiveWorkspace: PropTypes.func.isRequired,
};

export default connect(
  state => state.workspaces,
  {
    push,
    changeActiveWorkspace,
  },
)(Workspaces);
