import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import TopicPermissionsCard from '../TopicPermissionsCard';
import { KafkaTopic, TopicGrant, SecurityGroup } from '../../../../../models/Workspace';

describe('TopicPermissionsCard', () => {
  it('renders correctly', () => {
    const props = {
      readonly: true,
      topic: {
        id: 111,
        name: 'ruud',
        partitions: 1,
        replication_factor: 1,
        managing_role: {
          id: 1,
          group: {
            common_name: 'group',
            distinguished_name: 'distinguished',
          } as SecurityGroup,
          actions: 'action',
        } as TopicGrant,
        readonly_role: {
          id: 1,
          group: {
            common_name: 'group',
            distinguished_name: 'distinguished',
          } as SecurityGroup,
          actions: 'action',
        } as TopicGrant,
      } as KafkaTopic,
      onAddMember: () => null,
      onChangeMemberRole: () => null,
      removeMember: () => null,
    };
    const wrapper = shallow(<TopicPermissionsCard {...props} />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
