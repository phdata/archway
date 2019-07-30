import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import ManageCard from '../ManageCard';
import { ModalType } from '../../../../../constants';

describe('ManageCard', () => {
  it('it renders correctly', () => {
    const props = {
      title: 'Provision',
      buttonText: 'Provision',
      modalType: ModalType.ProvisionWorkspace,
      disabled: false,
      showModal: () => null,
    };
    const wrapper = shallow(<ManageCard {...props} />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
