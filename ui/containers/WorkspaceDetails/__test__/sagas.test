(global as any).config = { baseUrl: '' };

import { put, all, call, select } from 'redux-saga/effects';
import { cloneableGenerator } from 'redux-saga/utils';
import { Map } from 'immutable';
import {
  simpleMemberRequestComplete,
  setMembers,
  REQUEST_REMOVE_MEMBER,
  removeMemberSuccess,
  setMemberLoading,
} from '../actions';
import * as Api from '../../../service/api';
import { simpleMemberRequested, detailExtractor, memberRequestFormExtractor, removeMemberRequested } from '../sagas';
import { Workspace } from '../../../models/Workspace';
import { TOKEN_EXTRACTOR } from '../../../constants';

const details: Workspace = {
  id: 1,
  single_user: false,
  summary: 'Consolidated insights from social media interaction s.',
  applications: [],
  topics: [],
  name: 'Social Media Aggregation',
  approvals: {
    risk: {
      approver: 'benny',
      approval_time: new Date('2018-10-05T09:05:36.387Z'),
    },
    infra: {
      approver: 'benny',
      approval_time: new Date('2018-10-05T09:08:40.891Z'),
    },
  },
  behavior: 'simple',
  status: 'Approved',
  processing: [],
  compliance: {
    phi_data: false,
    pci_data: false,
    pii_data: true,
  },
  requester: 'CN=benny,CN=Users,DC=jotunn,DC=io',
  description:
    'All of our social media outlets gathered in one central place to generate insights and eventually new sales.',
  requested_date: new Date('2018-10-05T09:04:04.325Z'),
  data: [
    {
      id: 2,
      name: 'sw_social_media_aggregation',
      location: '/data/shared_workspace/social_media_aggregation',
      protocol: 'hdfs',
      size_in_gb: 1,
      consumed_in_gb: 0,
      managing_group: {
        group: {
          common_name: 'edh_sw_social_media_aggregation',
          distinguished_name: 'cn=edh_sw_social_media_aggregation,ou=heimdali,DC=jotunn,DC=io',
          sentry_role: 'role_sw_social_media_aggregation',
          attributes: [['dn', 'cn=edh_sw_social_media_aggregation,ou=heimdali,DC=jotunn,DC=io']],
        },
      },
      readwrite_group: {
        group: {
          common_name: 'edh_sw_social_media_aggregation_rw',
          distinguished_name: 'cn=edh_sw_social_media_aggregation_rw,ou=heimdali,DC=jotunn,DC=io',
          sentry_role: 'role_sw_social_media_aggregation_rw',
          attributes: [['dn', 'cn=edh_sw_social_media_aggregation_rw,ou=heimdali,DC=jotunn,DC=io']],
        },
      },
      readonly_group: {
        group: {
          common_name: 'edh_sw_social_media_aggregation_ro',
          distinguished_name: 'cn=edh_sw_social_media_aggregation_ro,ou=heimdali,DC=jotunn,DC=io',
          sentry_role: 'role_sw_social_media_aggregation_ro',
          attributes: [['dn', 'cn=edh_sw_social_media_aggregation_ro,ou=heimdali,DC=jotunn,DC=io']],
        },
      },
    },
  ],
};

describe('new member flow', () => {
  const generator = cloneableGenerator(simpleMemberRequested)({ type: 'SIMPLE_MEMBER_REQUEST', resource: 'data' });

  test('add user success', () => {
    const clone = generator.clone();
    expect(clone.next().value).toEqual(select(TOKEN_EXTRACTOR));
    expect(clone.next('abc').value).toEqual(select(detailExtractor));
    expect(clone.next(Map(details)).value).toEqual(select(memberRequestFormExtractor));
    expect(clone.next(Map({ username: 'username', role: 'readonly' })).value).toEqual(
      all([call(Api.newWorkspaceMember, 'abc', 1, 'data', 2, 'readonly', 'username')])
    );
    expect(clone.next().value).toEqual(put(simpleMemberRequestComplete()));
    expect(clone.next().value).toEqual(call(Api.getMembers, 'abc', 1));
    expect(clone.next([]).value).toEqual(put(setMembers([])));
    expect(clone.next().value).toEqual(put(setMemberLoading(false)));
  });
});

describe('remove member flow', () => {
  const generator = cloneableGenerator(removeMemberRequested)({
    type: REQUEST_REMOVE_MEMBER,
    distinguished_name: 'username',
    roleId: 1,
    resource: 'data',
  });

  test('remove member success', () => {
    const clone = generator.clone();
    expect(clone.next().value).toEqual(select(TOKEN_EXTRACTOR));
    expect(clone.next('abc').value).toEqual(select(detailExtractor));
    expect(clone.next(Map(details)).value).toEqual(
      all([call(Api.removeWorkspaceMember, 'abc', 1, 'data', 2, 'readonly', 'username')])
    );
    expect(clone.next().value).toEqual(put(removeMemberSuccess('username')));
    expect(clone.next().value).toEqual(call(Api.getMembers, 'abc', 1));
    expect(clone.next([]).value).toEqual(put(setMembers([])));
  });
});
