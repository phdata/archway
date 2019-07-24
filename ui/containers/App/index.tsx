import * as React from 'react';
import { History } from 'history';
import { Icon } from 'antd';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import Login from '../Login';
import { Main } from '../../components';
import { getToken, isLoading } from '../../redux/selectors';
import { getVersionInfo } from '../Login/selectors';

/* tslint:disable:no-var-requires */
const router = require('connected-react-router/immutable');

interface Props {
  loading: boolean;
  token?: string;
  history: History;
  version: string;
}

const AppContainer = ({ loading, token, history, version }: Props) => {
  if (loading) {
    return <Icon type="loading" spin={true} style={{ fontSize: 64 }} />;
  } else if (token) {
    return (
      <router.ConnectedRouter history={history}>
        <Main version={version} />
      </router.ConnectedRouter>
    );
  }
  return <Login />;
};

const mapStateToProps = () =>
  createStructuredSelector({
    loading: isLoading(),
    token: getToken(),
    version: getVersionInfo(),
  });

const mapDispatchToProps = {};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(AppContainer);
