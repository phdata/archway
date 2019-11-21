import * as React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';

import DataTab from '../DataTab';
import { Workspace } from '../../../../../models/Workspace';
import { Cluster } from '../../../../../models/Cluster';
import { Profile } from '../../../../../models/Profile';
import { ProvisioningType } from '../../../../../constants';

describe('DataTab', () => {
  it('renders correctly', () => {
    const props = {
      workspace: {
        id: 0,
        name: '',
        summary: '',
        description: '',
        behavior: '',
        requested_date: new Date(0),
        requester: '',
        single_user: false,
        compliance: {
          phi_data: false,
          pci_data: false,
          pii_data: false,
        },
        data: [
          {
            id: 1,
            name: '',
            location: '',
            protocol: '',
            size_in_gb: 0,
            consumed_in_gb: 0,
            managing_group: {
              group: {
                common_name: '',
                distinguished_name: '',
                sentry_role: '',
                attributes: [['', '']],
              },
            },
          },
        ],
        processing: [
          {
            id: 0,
            pool_name: '',
            max_cores: 0,
            max_memory_in_gb: 0,
          },
        ],
        topics: [],
        applications: [],
      } as Workspace,
      profile: {
        name: 'rkaland',
        username: 'ruudkaland',
        distinguished_name: 'rk',
        permissions: {
          risk_management: true,
          platform_operations: true,
        },
      } as Profile,
      cluster: {
        id: 'cluster',
        name: 'Valhalla',
        cm_url: 'https://manager.valhalla.phdata.io:7183',
        services: {
          mgmt: {
            navigator: [
              {
                host: 'master3.valhalla.phdata.io',
                port: 7187,
              },
            ],
            state: 'STARTED',
            status: 'GOOD_HEALTH',
          },
          yarn: {
            resource_manager: [
              {
                host: 'master3.valhalla.phdata.io',
                port: 8088,
              },
            ],
            node_manager: [
              {
                host: 'worker4.valhalla.phdata.io',
                port: 8042,
              },
              {
                host: 'worker2.valhalla.phdata.io',
                port: 8042,
              },
              {
                host: 'worker1.valhalla.phdata.io',
                port: 8042,
              },
              {
                host: 'worker3.valhalla.phdata.io',
                port: 8042,
              },
              {
                host: 'edge4.valhalla.phdata.io',
                port: 8042,
              },
              {
                host: 'worker5.valhalla.phdata.io',
                port: 8042,
              },
            ],
            state: 'STARTED',
            status: 'GOOD_HEALTH',
          },
          hue: {
            load_balancer: [
              {
                host: 'master2.valhalla.phdata.io',
                port: 8088,
              },
            ],
            state: 'STARTED',
            status: 'GOOD_HEALTH',
          },
          hive: {
            thrift: [
              {
                host: 'master2.valhalla.phdata.io',
                port: 10000,
              },
              {
                host: 'master3.valhalla.phdata.io',
                port: 10000,
              },
            ],
            state: 'STARTED',
            status: 'GOOD_HEALTH',
          },
          impala: {
            hiveServer2: [
              {
                host: 'worker1.valhalla.phdata.io',
                port: 21050,
              },
              {
                host: 'worker3.valhalla.phdata.io',
                port: 21050,
              },
              {
                host: 'worker5.valhalla.phdata.io',
                port: 21050,
              },
              {
                host: 'worker2.valhalla.phdata.io',
                port: 21050,
              },
              {
                host: 'worker4.valhalla.phdata.io',
                port: 21050,
              },
            ],
            beeswax: [
              {
                host: 'worker1.valhalla.phdata.io',
                port: 21000,
              },
              {
                host: 'worker3.valhalla.phdata.io',
                port: 21000,
              },
              {
                host: 'worker5.valhalla.phdata.io',
                port: 21000,
              },
              {
                host: 'worker2.valhalla.phdata.io',
                port: 21000,
              },
              {
                host: 'worker4.valhalla.phdata.io',
                port: 21000,
              },
            ],
            state: 'STARTED',
            status: 'GOOD_HEALTH',
          },
        },
        distribution: {
          name: 'CDH',
          version: '6.1.1',
        },
        status: 'GOOD_HEALTH',
      } as Cluster,
      memberLoading: false,
      provisioning: ProvisioningType.Pending,
      isPlatformOperations: true,
      showModal: () => null,
      onChangeAllocation: () => null,
      onChangeMemberRole: () => null,
      requestRefreshHiveTables: () => null,
      removeMember: () => null,
    };
    const wrapper = shallow(<DataTab {...props} />);

    expect(toJson(wrapper)).toMatchSnapshot();
  });
});
