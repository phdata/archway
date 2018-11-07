import { createSelector } from 'reselect';
import { workspaceListSelector, authSelector, clusterSelector } from '../../selectors';
import { Workspace } from '../../types/Workspace';
import { Profile } from '../../types/Profile';
import { Cluster } from '../../types/Cluster';

const fuseList = () => createSelector(
  workspaceListSelector,
  (listingState) => listingState.get('allWorkspaces'),
);

export const getListFilters = () => createSelector(
  workspaceListSelector,
  (listingState) => listingState.get('filters').toJS(),
);

export const workspaceList = () => createSelector(
  fuseList(),
  getListFilters(),
  (fuse, filters: { filter: string, behaviors: string[], statuses: string[] }) => {
    return (filters.filter ? fuse.search(filters.filter) : fuse.list)
      .filter((workspace: Workspace) => filters.behaviors.indexOf(workspace.behavior) >= 0)
      .filter((workspace: Workspace) => filters.statuses.indexOf(workspace.status || '') >= 0);
  },
);

export const isFetchingWorkspaces = () => createSelector(
  workspaceListSelector,
  (listingState) => listingState.get('fetching'),
);

export const getProfile = () => createSelector(
  authSelector,
  (authState) => authState.get('profile') as Profile,
);

export const getCluster = () => createSelector(
  clusterSelector,
  (clusterState) => clusterState.get('details').toJS() as Cluster,
);
