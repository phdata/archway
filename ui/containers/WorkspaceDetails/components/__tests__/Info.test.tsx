import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import Info from '../Info';

describe('Info', () => {
  it('renders correctly', () => {
    const props = {
      behavior: 'info',
      name: 'rkaland',
      summary: 'info-rkaland',
    };
    const wrapper = shallow(<Info {...props} />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
