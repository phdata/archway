import { createSelector } from 'reselect';
import * as selectors from '../../selectors';
import { RequestInput } from '../../types/RequestInput';
import { Workspace } from '../../types/Workspace';

export const getBehavior = () => createSelector(
  selectors.requestSelector,
  (requestState) => requestState.get('behavior'),
);

export const getRequest = () => createSelector(
  selectors.requestSelector,
  (requestState) => requestState.get('request') && requestState.get('request') as RequestInput,
);

export const getSelectedPage = () => createSelector(
  selectors.requestSelector,
  (requestState) => requestState.get('page'),
);

export const getGeneratedWorkspace = () => createSelector(
  selectors.requestSelector,
  (requestState) => requestState.get('workspace') && requestState.get('workspace').toJS() as Workspace,
);

export const isReady = () => createSelector(
  selectors.requestSelector,
  (requestState) => {
    switch (requestState.get('page')) {
      case 1:
        return !!requestState.get('behavior');
      case 2:
        return !!requestState.get('request');
      default:
        return false;
    }
  },
);

export const isCompleteEnabled = () => createSelector(
  selectors.requestSelector,
  (requestState) =>
    requestState.get('page') === 3 && !!requestState.get('behavior') && !!requestState.get('request'),
);
