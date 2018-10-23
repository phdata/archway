import * as React from 'react';
import { shallow } from 'enzyme';

import PrepareHelp from '../PrepareHelp';

describe('PrepareHelp', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<PrepareHelp location="" namespace="" />);
    expect(wrapper).toMatchSnapshot();
  });
});
