import { fromJS } from 'immutable';
import { createSelector } from 'reselect';
import { authSelector, clusterSelector, workspaceSelector } from '../../redux/selectors';
import {
  Member,
  NamespaceInfoList,
  ResourcePoolsInfo,
  Workspace,
  UserSuggestions,
  HiveAllocation,
} from '../../models/Workspace';
import { Cluster } from '../../models/Cluster';
import { Profile } from '../../models/Profile';

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

const dataFilter = (member: Member) =>
  Object.keys(member.data).length !== 0;

export const getMembers = () => createSelector(
  workspaceSelector,
  (workspaceState) =>
    (workspaceState.get('members') && workspaceState.get('details') &&
      workspaceState.get('members').toJS().filter(liasionFilter(workspaceState)).filter(dataFilter) as Member[]),
);

export const getNamespaceInfo = () => createSelector(
  workspaceSelector,
  (workspaceState) =>
    workspaceState.get('namespaceInfo') && workspaceState.get('namespaceInfo').toJS() as NamespaceInfoList,
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

export const getActiveTopic = () => createSelector(
  workspaceSelector,
  (workspaceState) =>
    workspaceState.get('activeTopic'),
);

export const getActiveApplication = () => createSelector(
  workspaceSelector,
  (workspaceState) =>
    workspaceState.get('activeApplication'),
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
