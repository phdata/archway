import { History } from 'history';
import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import Login from './Auth/Login';
import {Icon} from 'antd';
import Main from './Main';
import { getToken, isLoading } from './selectors';

const router = require('connected-react-router/immutable');


interface Props {
  loading: Boolean
  token?: String
  history: History
}

const AppContainer = ({ loading, token, history }: Props) => {
  if (loading) {
    return <Icon type="loading" spin={true} style={{ fontSize: 64 }} />;
  } else if (token) {
    return (
      <router.ConnectedRouter history={history}>
        <Main />
      </router.ConnectedRouter>
    );
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
