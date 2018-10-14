import * as React from 'react';
import { shallow } from 'enzyme';

import KafkaDetails from '../KafkaDetails';

describe('KafkaDetails', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<KafkaDetails consumerGroup="" topics={[]} showModal={() => null} />);
    expect(wrapper).toMatchSnapshot();
  });
});
