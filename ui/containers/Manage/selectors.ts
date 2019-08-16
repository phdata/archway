import { createSelector } from 'reselect';

import { manageSelector } from '../../redux/selectors';

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
