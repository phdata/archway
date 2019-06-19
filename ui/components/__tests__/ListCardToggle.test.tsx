import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import ListCardToggle from '../ListCardToggle';

describe('ListCardToggle', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<ListCardToggle selectedMode="cards" />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
