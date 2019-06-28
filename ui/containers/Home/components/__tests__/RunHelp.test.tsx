import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import RunHelp from '../RunHelp';

describe('RunHelp', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<RunHelp queue="root.users.ruud_kaland_rkaland" />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
