import * as React from 'react';
import { shallow } from 'enzyme';

import ApprovalDetails from '../ApprovalDetails';

describe('ApprovalDetails', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<ApprovalDetails />);
    expect(wrapper).toMatchSnapshot();
  });
});
