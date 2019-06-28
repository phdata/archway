import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import PrepareHelp from '../PrepareHelp';

describe('PrepareHelp', () => {
  it('renders correctly', () => {
    const props = {
      namespace: 'user_ruud_kaland_rkaland',
      location: 'hdfs://valhalla/user/ruud_kaland_rkaland/db',
    };
    const wrapper = shallow(<PrepareHelp {...props} />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
