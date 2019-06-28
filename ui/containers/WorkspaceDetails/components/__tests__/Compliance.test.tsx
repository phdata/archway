import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import Compliance from '../Compliance';

describe('Compliance', () => {
  it('renders correctly', () => {
    const props = {
      pci: true,
      phi: true,
      pii: true,
    };
    const wrapper = shallow(<Compliance {...props} />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
