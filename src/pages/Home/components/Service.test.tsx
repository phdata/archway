import * as React from 'react';
import { shallow } from 'enzyme';

import Service from './Service';
import { HiveServiceLinks, Status, Statusable } from '../../../types/Cluster';

describe('Service', () => {
  it('renders correctly', () => {
    const status = new Status<Statusable>({
      status: '',
    });
    const links = new HiveServiceLinks({
      status: '',
    });
    const wrapper = shallow(
      <Service
        name=""
        status={status}
        links={links}
        index={0}
      />,
    );
    expect(wrapper).toMatchSnapshot();
  });
});
