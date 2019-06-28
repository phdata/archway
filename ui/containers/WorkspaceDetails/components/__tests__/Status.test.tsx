import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import Status from '../Status';

describe('Status', () => {
  it('renders correctly', () => {
    const props = {
      ready: true,
      createdAt: new Date('2019-06-20T20:52:47Z'),
    };
    const wrapper = shallow(<Status {...props} />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
