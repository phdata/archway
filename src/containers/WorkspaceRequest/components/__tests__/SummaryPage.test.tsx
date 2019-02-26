import * as React from 'react';
import { shallow } from 'enzyme';

import SummaryPage from '../SummaryPage';
import { Profile } from '../../../../models/Profile';

describe('SummaryPage', () => {
  it('renders correctly', () => {
    const profile: Profile = {
      name: '',
      username: '',
      permissions: {
        risk_management: false,
        platform_operations: false,
      },
    };
    const wrapper = shallow(<SummaryPage profile={profile} />);
    expect(wrapper).toMatchSnapshot();
  });
});
