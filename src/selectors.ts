import { createSelector } from 'reselect';
import { Profile } from './Home/a.d';

export const getAuth = () => (state: any) => state.get('auth');
export const getCluster = () => (state: any) => state.get('cluster');
export const getRequest = () => (state: any) => state.get('request');

export const isLoading = () => createSelector(
    getAuth(),
    authState => authState.get('loading')
);

export const getToken = () => createSelector(
  getAuth(),
  authState => authState.get('token')
)

export const getProfile = () => createSelector(
  getAuth(),
  authState => authState.get('profile') as Profile
)