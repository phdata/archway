import * as React from 'react';
import { Route } from 'react-router-dom';
import { createStructuredSelector } from 'reselect';
import { connect } from 'react-redux';
import { RouteProps, Redirect } from 'react-router';

import { getProfile } from '../../redux/selectors';
import { Profile } from '../../models/Profile';

interface Props extends RouteProps {
  profile: Profile;
}

const OpsRoute = (props: Props) =>
  props.profile && props.profile.permissions.platform_operations ? <Route {...props} /> : <Redirect to="/home" />;

const mapStateToProps = () =>
  createStructuredSelector({
    profile: getProfile(),
  });

export default connect(mapStateToProps)(OpsRoute);
