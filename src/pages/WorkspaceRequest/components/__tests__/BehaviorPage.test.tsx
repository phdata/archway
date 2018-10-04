import * as React from 'react';
import { shallow } from 'enzyme';

import BehaviorPage from '../BehaviorPage';

describe('BehaviorPage', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<BehaviorPage onChange={() => null} />);
    expect(wrapper).toMatchSnapshot();
  });
});
