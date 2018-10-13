import * as React from 'react';
import { shallow } from 'enzyme';

import WorkspaceListItem from '../WorkspaceListItem';
import { Workspace } from '../../types/Workspace';

describe('WorkspaceListItem', () => {
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
      data: [{
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
          },
        },
      }],
      processing: [{
        id: 0,
        pool_name: '',
        max_cores: 0,
        max_memory_in_gb: 0,
      }],
      topics: [],
      applications: [],
    };
    const wrapper = shallow(<WorkspaceListItem workspace={workspace} onSelected={() => null} />);
    expect(wrapper).toMatchSnapshot();
  });
});
