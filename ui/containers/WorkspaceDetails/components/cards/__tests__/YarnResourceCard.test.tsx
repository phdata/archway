import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import YarnResourceCard from '../YarnResourceCard';

describe('YarnResourceCard', () => {
  it('renders correctly', () => {
    const props = {
      resource: {
        id: 1,
        pool_name: 'pool',
        max_cores: 4,
        max_memory_in_gb: 4,
      },
      showModal: () => null,
    };
    const wrapper = shallow(<YarnResourceCard {...props} />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
