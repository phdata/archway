import * as React from 'react';
import { shallow } from 'enzyme';

import Behavior from '../Behavior';

describe('Behavior', () => {
  it('renders correctly', () => {
    const wrapper = shallow(
      <Behavior
        behaviorKey=""
        icon=""
        title=""
        onChange={() => null}
      />,
    );
    expect(wrapper).toMatchSnapshot();
  });
});
