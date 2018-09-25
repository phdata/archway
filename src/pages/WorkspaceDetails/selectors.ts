import { createSelector } from 'reselect';
import { authSelector, clusterSelector, workspaceSelector } from '../../selectors';
import { Workspace, Member, HiveTable, YarnApplication } from '../../types/Workspace';

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

export const getHiveTables = () => createSelector(
  workspaceSelector,
  (workspaceState) => workspaceState.get('hiveTables') && workspaceState.get('hiveTables').toJS() as HiveTable[],
);

export const getApplications = () => createSelector(
  workspaceSelector,
  (workspaceState) => workspaceState.get('applications') && workspaceState.get('applications').toJS() as YarnApplication[],
);
