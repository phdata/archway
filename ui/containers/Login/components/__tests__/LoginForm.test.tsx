import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import LoginForm from '../LoginForm';

describe('LoginForm', () => {
  it('renders correctly', () => {
    const wrapper = shallow(<LoginForm onSubmit={() => null} loggingIn={false} error="" authType="" />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
