import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import Label from '../Label';

describe('Label', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<Label children="Label" />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
