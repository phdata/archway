import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import WorkspaceList from '../WorkspaceList';

describe('WorkspaceList', () => {
  it('it renders correctly', () => {
    const props = {
      workspaceList: [
        {
          id: 1,
          name: 'workspace',
          summary: 'summary',
          status: 'pending',
          behavior: 'simple',
          date_requested: new Date(0),
          date_fully_approved: new Date(0),
          total_disk_allocated_in_gb: 1024,
          total_max_cores: 4,
          total_max_memory_in_gb: 1024,
        },
      ],
      listingMode: 'card',
      emptyText: 'empty',
      fetching: true,
      setListingMode: () => null,
      openWorkspace: () => null,
    };
    const wrapper = shallow(<WorkspaceList {...props} />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
