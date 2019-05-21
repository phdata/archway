import * as React from 'react';
import { shallow } from 'enzyme';

import WorkspaceListItem from '../WorkspaceListItem';
import { WorkspaceSearchResult } from '../../models/Workspace';

describe('WorkspaceListItem', () => {
  it('renders correctly', () => {
    const workspace: WorkspaceSearchResult = {
      id: 0,
      name: '',
      summary: '',
      behavior: 'simple',
      status: 'Approved',
      date_requested: new Date(0),
      date_fully_approved: new Date(0),
      total_disk_allocated_in_gb: 0,
      total_max_cores: 0,
      total_max_memory_in_gb: 0,
    };
    const wrapper = shallow(<WorkspaceListItem workspace={workspace} onSelected={() => null} />);
    expect(wrapper).toMatchSnapshot();
  });
});
