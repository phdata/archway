import { createSelector } from 'reselect';
import { getAuth, getCluster } from '../selectors';
import { Cluster } from './a.d';
import { Workspace } from '../WorkspaceListing/Workspace';

export const getClusterInfo = () => createSelector(
  getCluster(),
  clusterState => clusterState.get('details').toJS() as Cluster
);

export const getPersonalWorkspace = () => createSelector(
  getAuth(),
  authState => authState.get('workspace').toJS() as Workspace
);

export const isProfileLoading = () => createSelector(
  getAuth(),
  authState => authState.get('profileLoading')
);