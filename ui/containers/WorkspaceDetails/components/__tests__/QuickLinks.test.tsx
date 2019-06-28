import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import QuickLinks from '../QuickLinks';

describe('QuickLinks', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<QuickLinks />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
