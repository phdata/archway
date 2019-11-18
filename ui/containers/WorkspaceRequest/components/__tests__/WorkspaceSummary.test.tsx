import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import WorkspaceSummary from '../WorkspaceSummary';
import { Workspace } from '../../../../models/Workspace';

describe('WorkspaceSummary', () => {
  it('renders correctly', () => {
    const workspace: Workspace = {
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
          protocol: '',
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
    };
    const props = {
      workspace,
      expanded: true,
      onToggleExpand: () => null,
    };

    const wrapper = shallow(<WorkspaceSummary {...props} />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
