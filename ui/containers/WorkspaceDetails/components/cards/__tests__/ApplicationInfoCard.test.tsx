import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import ApplicationInfoCard from '../ApplicationsInfoCard';

describe('ApplicationInfoCard', () => {
  it('renders correctly', () => {
    const props = {
      application: {
        id: 67,
        name: 'default',
        consumer_group: 'test_workspace_default_cg',
        group: {
          common_name: 'test_workspace_default_cg',
          distinguished_name: 'cn=test_workspace_default_cg,ou=groups,ou=Heimdali,DC=phdata,DC=io',
          sentry_role: 'role_test_workspace_default_cg',
          attributes: [
            ['dn', 'cn=test_workspace_default_cg,ou=groups,ou=Heimdali,DC=phdata,DC=io'],
            ['objectClass', 'group'],
            ['objectClass', 'top'],
            ['sAMAccountName', 'test_workspace_default_cg'],
            ['cn', 'test_workspace_default_cg'],
            ['gidNumber', '1039960'],
          ],
        },
      },
    };
    const wrapper = shallow(<ApplicationInfoCard {...props} />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
