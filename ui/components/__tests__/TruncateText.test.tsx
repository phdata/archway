import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import TruncateText from '../TruncateText';

describe('TruncateText', () => {
  it('it renders correctly', () => {
    const props = {
      text: 'truncate',
      maxLine: 2,
      lineHeight: 100,
    };
    const wrapper = shallow(<TruncateText {...props} />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
