import { createSelector } from 'reselect';
import { authSelector, clusterSelector } from '../../redux/selectors';
import { Cluster } from '../../models/Cluster';
import { Workspace } from '../../models/Workspace';

export const getClusterInfo = () => createSelector(
  clusterSelector,
  (clusterState) => clusterState.get('details').toJS() as Cluster,
);

export const getPersonalWorkspace = () => createSelector(
  authSelector,
  (authState) => authState.get('workspace') && authState.get('workspace').toJS() as Workspace,
);

export const isProfileLoading = () => createSelector(
  authSelector,
  (authState) => authState.get('profileLoading'),
);
