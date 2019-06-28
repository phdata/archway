import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import ApplicationRequest from '../ApplicationRequest';

describe('ApplicationRequest', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<ApplicationRequest />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
