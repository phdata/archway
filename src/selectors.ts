import { createSelector } from 'reselect';

export const getAuth = () => (state: any) => state.get('auth');
export const getCluster = (state: any) => state.get('cluster');

export const isLoading = () => createSelector(
    getAuth(),
    authState => authState.get('loading')
);

export const getToken = () => createSelector(
  getAuth(),
  authState => authState.get('token')
)