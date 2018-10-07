import * as React from 'react';
import { shallow } from 'enzyme';

import DescriptionDisplay from '../DescriptionDisplay';

describe('DescriptionDisplay', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<DescriptionDisplay description="" />);
    expect(wrapper).toMatchSnapshot();
  });
});
