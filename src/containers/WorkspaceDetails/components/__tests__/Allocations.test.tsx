import * as React from 'react';
import { shallow } from 'enzyme';

import Allocations from '../Allocations';

describe('Allocations', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<Allocations allocations={[]} onChangeAllocation={() => null} />);
    expect(wrapper).toMatchSnapshot();
  });
});
