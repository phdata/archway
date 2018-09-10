import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import Login from './Auth/Login';
import Spinner from './Common/Spinner';
import Main from './Main';
import { isLoading, getToken } from './selectors';


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

const mapStateToProps = () =>
  createStructuredSelector({
    loading: isLoading(),
    token: getToken(),
  });

const mapDispatchToProps = {};

export default connect(mapStateToProps, mapDispatchToProps)(AppContainer);
