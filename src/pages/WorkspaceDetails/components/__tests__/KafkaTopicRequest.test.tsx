import * as React from 'react';
import { shallow } from 'enzyme';

import KafkaTopicRequest from '../KafkaTopicRequest';

describe('KafkaTopicRequest', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<KafkaTopicRequest />);
    expect(wrapper).toMatchSnapshot();
  });
});
