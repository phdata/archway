import { createSelector } from 'reselect';
import { authSelector, clusterSelector, homeSelector } from '../../redux/selectors';
import { Cluster } from '../../models/Cluster';
import { Workspace } from '../../models/Workspace';

export const getClusterInfo = () =>
  createSelector(
    clusterSelector,
    clusterState => clusterState.get('details').toJS() as Cluster
  );

export const isClusterLoading = () =>
  createSelector(
    clusterSelector,
    clusterState => clusterState.get('loading')
  );

export const getPersonalWorkspace = () =>
  createSelector(
    authSelector,
    authState => authState.get('workspace') && (authState.get('workspace').toJS() as Workspace)
  );

export const getRecentWorkspaces = () =>
  createSelector(
    homeSelector,
    listState => listState.get('recent') && (listState.get('recent').toJS() as Workspace[])
  );

export const isProfileLoading = () =>
  createSelector(
    authSelector,
    authState => authState.get('profileLoading')
  );

export const getProvisioning = () =>
  createSelector(
    authSelector,
    authState => authState.get('provisioning')
  );
