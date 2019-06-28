import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import MessagingTab from '../MessagingTab';
import { Workspace } from '../../../../../models/Workspace';
import { Profile } from '../../../../../models/Profile';

describe('MessagingTab', () => {
  it('renders correctly', () => {
    const props = {
      workspace: {
        id: 0,
        name: '',
        summary: '',
        description: '',
        behavior: '',
        requested_date: new Date(0),
        requester: '',
        single_user: false,
        compliance: {
          phi_data: false,
          pci_data: false,
          pii_data: false,
        },
        data: [
          {
            id: 1,
            name: '',
            location: '',
            size_in_gb: 0,
            consumed_in_gb: 0,
            managing_group: {
              group: {
                common_name: '',
                distinguished_name: '',
                sentry_role: '',
                attributes: [['', '']],
              },
            },
          },
        ],
        processing: [
          {
            id: 0,
            pool_name: '',
            max_cores: 0,
            max_memory_in_gb: 0,
          },
        ],
        topics: [],
        applications: [],
      } as Workspace,
      profile: {
        name: 'rkaland',
        username: 'ruudkaland',
        distinguished_name: 'rk',
        permissions: {
          risk_management: true,
          platform_operations: true,
        },
      } as Profile,
      onAddTopic: () => null,
      onAddMember: () => null,
      onChangeMemberRole: () => null,
      removeMember: () => null,
    };
    const wrapper = shallow(<MessagingTab {...props} />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
