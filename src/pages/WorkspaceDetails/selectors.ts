import { createSelector } from 'reselect';
import { authSelector, clusterSelector, workspaceSelector } from '../../selectors';
import { Member, NamespaceInfo, PoolInfo, Workspace } from '../../types/Workspace';
import { Cluster } from '../../types/Cluster';
import { Profile } from '../../types/Profile';

export const getWorkspace = () => createSelector(
  workspaceSelector,
  (workspaceState) =>
    workspaceState.get('details') && workspaceState.get('details').toJS() as Workspace,
);

export const getClusterDetails = () => createSelector(
  clusterSelector,
  (clusterState) =>
    clusterState.get('details').toJS() as Cluster,
);

export const getProfile = () => createSelector(
  authSelector,
  (authState) =>
    authState.get('profile') as Profile,
);

const liasionFilter = (state: any) => (member: Member) =>
  member.username !== (state.get('details').toJS() as Workspace).requester &&
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
    workspaceState.get('namespaceInfo') && workspaceState.get('namespaceInfo').toJS() as  NamespaceInfo[],
);

export const getPoolInfo = () => createSelector(
  workspaceSelector,
  (workspaceState) =>
    workspaceState.get('resourcePools') && workspaceState.get('resourcePools').toJS() as PoolInfo[],
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
