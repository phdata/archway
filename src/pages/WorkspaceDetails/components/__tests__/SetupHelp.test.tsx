import * as React from 'react';
import { shallow } from 'enzyme';

import SetupHelp from '../SetupHelp';

describe('SetupHelp', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<SetupHelp />);
    expect(wrapper).toMatchSnapshot();
  });
});
