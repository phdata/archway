import * as React from 'react';
import { shallow } from 'enzyme';

import OverviewPage from '../OverviewPage';

describe('OverviewPage', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<OverviewPage setRequest={() => null} />);
    expect(wrapper).toMatchSnapshot();
  });
});
