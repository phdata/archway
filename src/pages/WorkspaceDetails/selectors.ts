import { createSelector } from 'reselect';
import { authSelector, clusterSelector, workspaceSelector } from '../../selectors';
import { Workspace } from '../../types/Workspace';

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
