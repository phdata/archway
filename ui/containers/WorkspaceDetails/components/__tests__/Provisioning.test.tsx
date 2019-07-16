import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import Provisioning from '../Provisioning';
import { ProvisioningType } from '../../../../constants';

describe('Provisioning', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<Provisioning provisioning={ProvisioningType.Complete} />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
