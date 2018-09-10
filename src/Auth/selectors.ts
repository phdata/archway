import { createSelector } from 'reselect';
import { getAuth } from '../selectors';

export const isLoggingIn = () => createSelector(
  getAuth(),
  authState => authState.get('loading')
);

export const loginError = () => createSelector(
  getAuth(),
  authState => authState.get('error')
);