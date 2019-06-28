import * as React from 'react';
import renderer from 'react-test-renderer';

import BehaviorPage from '../BehaviorPage';

describe('BehaviorPage', () => {
  it('renders correctly', () => {
    const wrapper = renderer.create(<BehaviorPage onChange={() => null} importData={() => null} />).toJSON();
    expect(wrapper).toMatchSnapshot();
  });
});
