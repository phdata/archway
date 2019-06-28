import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import InstructionCard from '../InstructionCard';

describe('InstructionCard', () => {
  it('renders correctly', () => {
    const props = {
      location: 'netherlands',
      namespace: 'valhalla',
      host: 'https://...',
      port: 8080,
      queue: 'queue',
    };
    const wrapper = shallow(<InstructionCard {...props} />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
