import { createSelector } from 'reselect';

import { manageSelector, authSelector } from '../../redux/selectors';

export const getCompliances = () =>
  createSelector(
    manageSelector,
    manageState => manageState.get('compliances').toJS()
  );

export const getSelectedCompliance = () =>
  createSelector(
    manageSelector,
    manageState => manageState.get('selectedCompliance').toJS()
  );

export const isLoading = () =>
  createSelector(
    manageSelector,
    manageState => manageState.get('loading')
  );

export const getRequester = () =>
  createSelector(
    authSelector,
    authState => authState.get('profile') && authState.get('profile').username
  );
