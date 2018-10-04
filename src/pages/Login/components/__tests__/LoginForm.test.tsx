import * as React from 'react';
import { shallow } from 'enzyme';

import LoginForm from '../LoginForm';

describe('LoginForm', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<LoginForm onSubmit={() => null} loggingIn={false} error="" />);
    expect(wrapper).toMatchSnapshot();
  });
});
