import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import CreateHelp from '../CreateHelp';

describe('CreateHelp', () => {
  it('renders correctly', () => {
    const props = {
      host: 'host',
      port: 8080,
      namespace: 'hdfs',
    };
    const wrapper = shallow(<CreateHelp {...props} />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
