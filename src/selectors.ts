import { createSelector } from 'reselect';
import { Profile } from './types/Profile';

export const authSelector = (state: any) => state.get('login');
export const clusterSelector = (state: any) => state.get('cluster');
export const requestSelector = (state: any) => state.get('request');
export const workspaceSelector = (state: any) => state.get('details');
export const workspaceListSelector = (state: any) => state.get('listing');

export const isLoading = () => createSelector(
    authSelector,
    (authState) => authState.get('loading'),
);

export const getToken = () => createSelector(
  authSelector,
  (authState) => authState.get('token'),
);

export const getProfile = () => createSelector(
  authSelector,
  (authState) => authState.get('profile') as Profile,
);
