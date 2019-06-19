import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import OverviewPage from '../OverviewPage';
import { RequestInput } from '../../../../models/RequestInput';

describe('OverviewPage', () => {
  it('renders correctly', () => {
    const request: RequestInput = {
      name: '',
      summary: '',
      description: '',
      compliance: {
        phi_data: false,
        pci_data: false,
        pii_data: false,
      },
    };
    const wrapper = shallow(<OverviewPage request={request} setRequest={() => null} />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
