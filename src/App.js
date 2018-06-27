import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import Login from './Auth/Login';
import Main from './Main';
import Spinner from './Common/Spinner';

const AppContainer = ({ loading, token, history }) => {
  if (loading) {
    return <Spinner />;
  } else if (token) {
    return <Main history={history} />;
  }
  return <Login />;
};

AppContainer.propTypes = {
  loading: PropTypes.bool.isRequired,
  token: PropTypes.string.isRequired,
  history: PropTypes.object.isRequired,
};

export default connect(
  state => state.auth,
  {},
)(AppContainer);
