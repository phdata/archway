import * as React from 'react';
import { connect } from 'react-redux';

import Login from './Auth/Login';
import Main from './Main';
import Spinner from './Common/Spinner';
import {StoreState} from './types';

interface Props {
  loading: Boolean
  token?: String
}

const AppContainer = ({ loading, token }: Props) => {
  if (loading) {
    return <Spinner />;
  } else if (token) {
    return <Main />;
  }
  return <Login />;
};

const mapStateToProps = (state: StoreState) => state.auth;

const mapDispatchToProps = {};

export default connect(mapStateToProps, mapDispatchToProps)(AppContainer);
