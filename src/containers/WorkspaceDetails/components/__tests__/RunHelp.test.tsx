import * as React from 'react';
import { shallow } from 'enzyme';

import RunHelp from '../RunHelp';

describe('RunHelp', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<RunHelp queue="" />);
    expect(wrapper).toMatchSnapshot();
  });
});
