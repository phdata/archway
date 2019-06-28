import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import CompliancePage from '../CompliancePage';

describe('CompliancePage', () => {
  it('renders correctly', () => {
    const props = {
      request: {
        name: 'request',
        summary: 'summary',
        description: 'desc',
        compliance: {
          phi_data: true,
          pci_data: true,
          pii_data: true,
        },
      },
      setRequest: () => null,
    };
    const wrapper = shallow(<CompliancePage {...props} />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
