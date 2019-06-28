import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import SimpleMemberRequest from '../SimpleMemberRequest';
import { HiveAllocation } from '../../../../../models/Workspace';

describe('SimpleMemberRequest', () => {
  it('renders correctly', () => {
    const props = {
      allocations: [
        {
          id: 161,
          name: 'sw_test_workspace',
          location: 'hdfs://valhalla/data/shared_workspace/test_workspace',
          size_in_gb: 1000,
          consumed_in_gb: 0,
        } as HiveAllocation,
      ],
    };
    const wrapper = shallow(<SimpleMemberRequest {...props} />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
