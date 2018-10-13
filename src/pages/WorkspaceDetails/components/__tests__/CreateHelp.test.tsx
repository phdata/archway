import * as React from 'react';
import { shallow } from 'enzyme';

import CreateHelp from '../CreateHelp';

describe('CreateHelp', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<CreateHelp host="" port={123} namespace="" />);
    expect(wrapper).toMatchSnapshot();
  });
});
