import * as React from 'react';
import { shallow } from 'enzyme';

import YarnDetails from '../YarnDetails';

describe('YarnDetails', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<YarnDetails poolName="" />);
    expect(wrapper).toMatchSnapshot();
  });
});
