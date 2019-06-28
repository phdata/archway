import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import TablesCard from '../TablesCard';

describe('TablesCard', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<TablesCard onRefreshHiveTables={() => null} />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
