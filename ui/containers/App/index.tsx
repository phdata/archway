import * as React from 'react';
import { History } from 'history';
import { Icon } from 'antd';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import Login from '../Login';
import { Main } from '../../components';
import { getToken, isLoading, getProfile } from '../../redux/selectors';
import { getVersionInfo } from '../Login/selectors';
import { Profile } from '../../models/Profile';

/* tslint:disable:no-var-requires */
const router = require('connected-react-router/immutable');

interface Props {
  loading: boolean;
  token?: string;
  history: History;
  version: string;
  profile: Profile;
}

const AppContainer = ({ loading, token, history, version, profile }: Props) => {
  if (loading) {
    return <Icon type="loading" spin={true} style={{ fontSize: 64 }} />;
  } else if (token) {
    return (
      <router.ConnectedRouter history={history}>
        <Main version={version} profile={profile} />
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
    profile: getProfile(),
  });

const mapDispatchToProps = {};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(AppContainer);
