import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import Liaison from '../Liaison';
import { Member } from '../../../../models/Workspace';

describe('Liaison', () => {
  it('renders correctly', () => {
    const data: Member = {
      name: 'hdfs',
      distinguished_name: 'rkaland',
      data: null,
      topics: null,
      applications: null,
    };
    const wrapper = shallow(<Liaison data={data} />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
