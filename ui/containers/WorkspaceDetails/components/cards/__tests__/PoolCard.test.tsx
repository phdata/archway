import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import PoolCard from '../PoolCard';
import { ResourcePool } from '../../../../../models/Workspace';

describe('PoolCard', () => {
  it('renders correctly', () => {
    const props = {
      data: [
        {
          id: 111,
          pool_name: 'RPool',
          max_cores: 4,
          max_memory_in_gb: 1000,
        } as ResourcePool,
      ],
    };
    const wrapper = shallow(<PoolCard {...props} />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
