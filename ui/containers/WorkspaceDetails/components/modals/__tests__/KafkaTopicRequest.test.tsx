import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import KafkaTopicRequest from '../KafkaTopicRequest';

describe('KafkaTopicRequest', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<KafkaTopicRequest />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
