import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import YarnApplicationsCard from '../YarnApplicationsCard';
import { ResourcePoolsInfo } from '../../../../../models/Workspace';

describe('YarnApplicationsCard', () => {
  it('renders correctly', () => {
    const props = {
      rmURL: 'https://',
      pools: {
        loading: false,
        data: [
          {
            resource_pool: 'rpool',
            applications: [
              {
                id: '1',
                name: 'rworkspace',
                start_time: new Date('2019-06-20T20:52:47Z'),
              },
            ],
          },
        ],
      } as ResourcePoolsInfo,
    };
    const wrapper = shallow(<YarnApplicationsCard {...props} />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
