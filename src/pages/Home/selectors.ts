import { createSelector } from 'reselect';
import { authSelector, clusterSelector } from '../../selectors';
import { Cluster } from '../../types/Cluster';
import { Workspace } from '../../types/Workspace';

export const getClusterInfo = () => createSelector(
  clusterSelector,
  clusterState => clusterState.get('details').toJS() as Cluster
);

export const getPersonalWorkspace = () => createSelector(
  authSelector,
  authState => authState.get('workspace').toJS() as Workspace
);

export const isProfileLoading = () => createSelector(
  authSelector,
  authState => authState.get('profileLoading')
);