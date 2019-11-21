import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import HiveDatabase from '../HiveDatabase';
import { HiveAllocation } from '../../../../models/Workspace';

describe('HiveDatabase', () => {
  it('renders correctly', () => {
    const hiveAllocation: HiveAllocation = {
      id: 161,
      name: 'sw_test_workspace',
      location: 'hdfs://valhalla/data/shared_workspace/test_workspace',
      protocol: 'hdfs',
      size_in_gb: 1000,
      consumed_in_gb: 0,
      managing_group: {
        location_access: new Date('2019-06-20T20:52:47Z'),
        database_access: new Date('2019-06-20T20:52:47Z'),
        group: {
          common_name: 'edh_sw_test_workspace',
          distinguished_name: 'cn=edh_sw_test_workspace,ou=groups,ou=Heimdali,DC=phdata,DC=io',
          sentry_role: 'role_sw_test_workspace',
          attributes: [
            ['dn', 'cn=edh_sw_test_workspace,ou=groups,ou=Heimdali,DC=phdata,DC=io'],
            ['objectClass', 'group'],
            ['objectClass', 'top'],
            ['sAMAccountName', 'edh_sw_test_workspace'],
            ['cn', 'edh_sw_test_workspace'],
            ['gidNumber', '1039957'],
          ],
        },
      },
      readwrite_group: {
        location_access: new Date('2019-06-20T20:52:47Z'),
        database_access: new Date('2019-06-20T20:52:47Z'),
        group: {
          common_name: 'edh_sw_test_workspace_rw',
          distinguished_name: 'cn=edh_sw_test_workspace_rw,ou=groups,ou=Heimdali,DC=phdata,DC=io',
          sentry_role: 'role_sw_test_workspace_rw',
          attributes: [
            ['dn', 'cn=edh_sw_test_workspace_rw,ou=groups,ou=Heimdali,DC=phdata,DC=io'],
            ['objectClass', 'group'],
            ['objectClass', 'top'],
            ['sAMAccountName', 'edh_sw_test_workspace_rw'],
            ['cn', 'edh_sw_test_workspace_rw'],
            ['gidNumber', '1039958'],
          ],
        },
      },
      readonly_group: {
        location_access: new Date('2019-06-20T20:52:47Z'),
        database_access: new Date('2019-06-20T20:52:47Z'),
        group: {
          common_name: 'edh_sw_test_workspace_ro',
          distinguished_name: 'cn=edh_sw_test_workspace_ro,ou=groups,ou=Heimdali,DC=phdata,DC=io',
          sentry_role: 'role_sw_test_workspace_ro',
          attributes: [
            ['dn', 'cn=edh_sw_test_workspace_ro,ou=groups,ou=Heimdali,DC=phdata,DC=io'],
            ['objectClass', 'group'],
            ['objectClass', 'top'],
            ['sAMAccountName', 'edh_sw_test_workspace_ro'],
            ['cn', 'edh_sw_test_workspace_ro'],
            ['gidNumber', '1039959'],
          ],
        },
      },
    };
    const props = {
      data: hiveAllocation,
      isDefault: true,
      isPlatformOperations: true,
      showModal: () => null,
    };
    const wrapper = shallow(<HiveDatabase {...props} />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
