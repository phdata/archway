import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import CardHeader from '../CardHeader';

describe('CardHeader', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<CardHeader children={null} />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
