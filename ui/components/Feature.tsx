import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';

import { getFeatureFlags } from '../redux/selectors';
import { FeatureFlagType } from '../constants';

interface Props {
  flag: FeatureFlagType;
  featureFlags: string[];
  children?: any;
}

const Feature = ({ flag, featureFlags, children }: Props) =>
  featureFlags.includes(flag) ? <React.Fragment>{children}</React.Fragment> : null;

const mapStateToProps = () =>
  createStructuredSelector({
    featureFlags: getFeatureFlags(),
  });

export default connect(mapStateToProps)(Feature);
