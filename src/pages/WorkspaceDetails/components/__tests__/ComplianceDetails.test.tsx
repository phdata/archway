import * as React from 'react';
import { shallow } from 'enzyme';

import ComplianceDetails from '../ComplianceDetails';

describe('ComplianceDetails', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<ComplianceDetails pci={false} phi={false} pii={false} />);
    expect(wrapper).toMatchSnapshot();
  });
});
