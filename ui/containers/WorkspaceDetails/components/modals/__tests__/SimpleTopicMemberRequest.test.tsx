import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import SimpleTopicMemberRequest from '../SimpleTopicMemberRequest';
import { UserSuggestions, UserSuggestion } from '../../../../../models/Workspace';

describe('SimpleTopicMemberRequest', () => {
  it('renders correctly', () => {
    const props = {
      suggestions: {
        filter: 'user',
        users: [
          {
            display: 'user_display',
            distinguished_name: 'user_d',
          } as UserSuggestion,
        ],
        groups: [
          {
            display: 'group_display',
            distinguished_name: 'group_d',
          } as UserSuggestion,
        ],
      } as UserSuggestions,
    };
    const wrapper = shallow(<SimpleTopicMemberRequest {...props} />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
