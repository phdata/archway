import { History } from 'history';
import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import Login from './pages/Login';
import {Icon} from 'antd';
import Main from './Main';
import { getToken, isLoading } from './selectors';

/* tslint:disable:no-var-requires */
const router = require('connected-react-router/immutable');

interface Props {
  loading: boolean;
  token?: string;
  history: History;
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
