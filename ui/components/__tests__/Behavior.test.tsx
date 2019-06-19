import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import Behavior from '../Behavior';

describe('Behavior', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<Behavior behaviorKey="" icon="" title="" onChange={() => null} />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
