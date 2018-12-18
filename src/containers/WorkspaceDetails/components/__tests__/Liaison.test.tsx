import * as React from 'react';
import { shallow } from 'enzyme';

import Liaison from '../Liaison';

describe('Liaison', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<Liaison liaison={undefined} />);
    expect(wrapper).toMatchSnapshot();
  });
});
