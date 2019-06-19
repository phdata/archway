import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import Service from '../Service';
import { HiveServiceLinks, Status, Statusable } from '../../../../models/Cluster';

describe('Service', () => {
  it('renders correctly', () => {
    const status = new Status<Statusable>({
      status: '',
    });
    const links = new HiveServiceLinks({
      status: '',
      thrift: [],
    });
    const wrapper = shallow(<Service name="" status={status} links={links} index={0} />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
