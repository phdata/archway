import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import TopicCard from '../TopicCard';
import { KafkaTopic, TopicGrant, SecurityGroup } from '../../../../../models/Workspace';

describe('TopicCard', () => {
  it('renders correctly', () => {
    const props = {
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
    };
    const wrapper = shallow(<TopicCard {...props} />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
