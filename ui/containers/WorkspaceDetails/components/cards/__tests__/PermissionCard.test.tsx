import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import PermissionCard from '../PermissionsCard';
import { HiveAllocation } from '../../../../../models/Workspace';

describe('PermissionCard', () => {
  it('renders correctly', () => {
    const props = {
      readonly: true,
      allocation: {
        id: 161,
        name: 'sw_test_workspace',
        location: 'hdfs://valhalla/data/shared_workspace/test_workspace',
        size_in_gb: 1000,
        consumed_in_gb: 0,
      } as HiveAllocation,
      memberLoading: false,
      onAddMember: () => null,
      onChangeMemberRole: () => null,
      removeMember: () => null,
    };
    const wrapper = shallow(<PermissionCard {...props} />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
