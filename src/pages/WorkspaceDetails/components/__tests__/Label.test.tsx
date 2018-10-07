import * as React from 'react';
import { shallow } from 'enzyme';

import Label from '../Label';

describe('Label', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<Label children={null} />);
    expect(wrapper).toMatchSnapshot();
  });
});
