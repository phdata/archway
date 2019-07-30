import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import ListingSearchBar from '../ListingSearchBar';

describe('ListingSearchBar', () => {
  it('it renders correctly', () => {
    const props = {
      filters: {
        filter: 'Test',
        behaviors: ['simple', 'structured', 'custom'],
        statuses: ['pending', 'rejected'],
      },
      updateFilter: () => null,
    };
    const wrapper = shallow(<ListingSearchBar {...props} />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
