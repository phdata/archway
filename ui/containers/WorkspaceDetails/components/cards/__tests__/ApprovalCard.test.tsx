import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import ApprovalCard from '../ApprovalCard';

describe('ApprovalCard', () => {
  it('renders correctly', () => {
    const props = {
      data: {
        approver: 'tforester',
        approval_time: new Date('2019-06-20T20:52:35Z'),
      },
    };
    const wrapper = shallow(<ApprovalCard {...props} />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
