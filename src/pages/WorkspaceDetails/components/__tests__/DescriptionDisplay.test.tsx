import * as React from 'react';
import { shallow } from 'enzyme';

import DescriptionDetails from '../DescriptionDetails';

describe('DescriptionDetails', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<DescriptionDetails description="" />);
    expect(wrapper).toMatchSnapshot();
  });
});
