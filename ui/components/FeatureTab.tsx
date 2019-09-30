import * as React from 'react';

import { Feature } from './';
import { FeatureFlagType } from '../constants';
import { Tabs } from 'antd';

interface Props {
  flag: FeatureFlagType;
  tab: string;
  key: string;
  children: any;
}

const FeatureTab: React.FunctionComponent<Props> = ({ flag, children, ...restProps }) => {
  return (
    <Feature flag={flag}>
      <Tabs.TabPane {...restProps}>{children}</Tabs.TabPane>
    </Feature>
  );
};

export default FeatureTab;
