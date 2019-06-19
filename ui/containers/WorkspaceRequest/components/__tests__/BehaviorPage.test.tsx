import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import BehaviorPage from '../BehaviorPage';

describe('BehaviorPage', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<BehaviorPage onChange={() => null} importData={() => null} />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
