import { createSelector } from 'reselect';
import { authSelector, homeSelector } from '../../redux/selectors';
import { Workspace } from '../../models/Workspace';

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

export const isWorkspaceFetched = () =>
  createSelector(
    authSelector,
    authState => authState.get('workspaceFetched')
  );
