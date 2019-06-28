import { shallow } from 'enzyme';
import * as React from 'react';
import toJson from 'enzyme-to-json';

import ComplianceCard from '../ComplianceCard';

describe('ComplianceCard', () => {
  it('renders correctly', () => {
    const props = {
      type: 'pci',
      data: {
        icon: 'bank',
        label: 'Payment Card Industry [Data Security Standard]',
        values: [
          {
            key: 'card',
            label: 'Full or partial credit card numbers?',
          },
          {
            key: 'bank',
            label: 'Full or partial bank account numbers?',
          },
          {
            key: 'other',
            label: 'Any other combination of data that can be used to make purchases?',
          },
        ],
      },
      values: [
        {
          key: 'card',
          label: 'Full or partial credit card numbers?',
        },
        {
          key: 'bank',
          label: 'Full or partial bank account numbers?',
        },
        {
          key: 'other',
          label: 'Any other combination of data that can be used to make purchases?',
        },
      ],
      onChange: () => null,
    };

    const wrapper = shallow(<ComplianceCard {...props} />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
