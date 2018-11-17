import { fromJS } from 'immutable';
import { createSelector } from 'reselect';
import { authSelector, clusterSelector, workspaceSelector } from '../../selectors';
import {
  Member,
  NamespaceInfo,
  ResourcePoolsInfo,
  Workspace,
  UserSuggestions,
  HiveAllocation,
} from '../../types/Workspace';
import { Cluster } from '../../types/Cluster';
import { Profile } from '../../types/Profile';

export const getWorkspace = () => createSelector(
  workspaceSelector,
  (workspaceState) =>
    workspaceState.get('details') && workspaceState.get('details').toJS() as Workspace,
);

export const getUserSuggestions = () => createSelector(
  workspaceSelector,
  (workspaceState) =>
    workspaceState.get('userSuggestions') && workspaceState.get('userSuggestions').toJS() as UserSuggestions,
);

export const getClusterDetails = () => createSelector(
  clusterSelector,
  (clusterState) =>
    clusterState.get('details').toJS() as Cluster,
);

export const getSelectedAllocation = () => createSelector(
  workspaceSelector,
  (workspaceState) =>
    (workspaceState.get('selectedAllocation') && workspaceState.get('selectedAllocation').toJS() as HiveAllocation)
    || workspaceState.getIn(['details', 'data', 0], fromJS({})).toJS() as HiveAllocation,
);

export const getProfile = () => createSelector(
  authSelector,
  (authState) =>
    authState.get('profile') as Profile,
);

const liasionFilter = (state: any) => (member: Member) =>
  member.distinguished_name !== (state.get('details').toJS() as Workspace).requester &&
  !(member.removeStatus || {}).success;

export const getMembers = () => createSelector(
  workspaceSelector,
  (workspaceState) =>
    (workspaceState.get('members') && workspaceState.get('details') &&
      workspaceState.get('members').toJS().filter(liasionFilter(workspaceState)) as Member[]),
);

export const getNamespaceInfo = () => createSelector(
  workspaceSelector,
  (workspaceState) =>
    workspaceState.get('namespaceInfo') && workspaceState.get('namespaceInfo').toJS() as NamespaceInfo[],
);

export const getPoolInfo = () => createSelector(
  workspaceSelector,
  (workspaceState) =>
    workspaceState.get('resourcePools') && workspaceState.get('resourcePools').toJS() as ResourcePoolsInfo,
);

export const getApproved = () => createSelector(
  workspaceSelector,
  (workspaceState) =>
    workspaceState.hasIn(['details', 'approvals', 'infra']) &&
    workspaceState.hasIn(['details', 'approvals', 'risk']),
);

export const getActiveModal = () => createSelector(
  workspaceSelector,
  (workspaceState) =>
    workspaceState.get('activeModal'),
);

const liasionFinder = (state: any) => (member: Member) =>
  member.distinguished_name === (state.get('details').toJS() as Workspace).requester;

export const getLiaison = () => createSelector(
  workspaceSelector,
  (workspaceState) =>
    (workspaceState.get('members') &&
      workspaceState.get('details') &&
      workspaceState.get('members').toJS().find(liasionFinder(workspaceState)) as Member),
);
