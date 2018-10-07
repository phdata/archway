import * as React from 'react';
import { shallow } from 'enzyme';

import HiveDetails from '../HiveDetails';

describe('HiveDetails', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<HiveDetails namespace="" />);
    expect(wrapper).toMatchSnapshot();
  });
});
