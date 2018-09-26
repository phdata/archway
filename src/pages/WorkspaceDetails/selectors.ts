import { createSelector } from 'reselect';
import { authSelector, clusterSelector, workspaceSelector } from '../../selectors';
import { Workspace, Member, YarnApplication, NamespaceInfo, PoolInfo } from '../../types/Workspace';

export const getWorkspace = () => createSelector(
  workspaceSelector,
  (workspaceState) => workspaceState.get('details') && workspaceState.get('details').toJS() as Workspace,
);

export const getClusterDetails = () => createSelector(
  clusterSelector,
  (workspaceState) => workspaceState.get('details'),
);

export const getProfile = () => createSelector(
  authSelector,
  (workspaceState) => workspaceState.get('details'),
);

export const getMembers = () => createSelector(
  workspaceSelector,
  (workspaceState) => workspaceState.get('members') && workspaceState.get('members').toJS() as Member[],
);

export const getNamespaceInfo = () => createSelector(
  workspaceSelector,
  (workspaceState) => workspaceState.get('namespaceInfo') && workspaceState.get('namespaceInfo').toJS() as  NamespaceInfo[]
);

export const getPoolInfo = () => createSelector(
  workspaceSelector,
  (workspaceState) => workspaceState.get('resourcePools') && workspaceState.get('resourcePools').toJS() as PoolInfo[],
);
