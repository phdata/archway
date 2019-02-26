import { createSelector } from 'reselect';
import * as selectors from '../../redux/selectors';
import { RequestInput } from '../../models/RequestInput';
import { Workspace } from '../../models/Workspace';
import { Profile } from '../../models/Profile';

export const getBehavior = () => createSelector(
  selectors.requestSelector,
  (requestState) => requestState.get('behavior'),
);

export const getRequest = () => createSelector(
  selectors.requestSelector,
  (requestState) => requestState.get('request') && requestState.get('request').toJS() as RequestInput,
);

export const getGeneratedWorkspace = () => createSelector(
  selectors.requestSelector,
  (requestState) => requestState.get('workspace') && requestState.get('workspace').toJS() as Workspace,
);

export const getProfile = () => createSelector(
  selectors.authSelector,
  (authState) =>
    authState.get('profile') as Profile,
);

export const getLoading = () => createSelector(
  selectors.requestSelector,
  (requestState) => requestState.get('loading'),
);

export const isNextStepEnabled = () => createSelector(
  selectors.requestSelector,
  (requestState) => {
    if (requestState.get('loading')) {
      return false;
    }

    switch (requestState.get('currentPage')) {
      case 1:
        return !!requestState.get('template');
      case 2:
      {
        const request: RequestInput = requestState.get('request').toJS();
        return !!request.name && !!request.summary && !!request.description;
      }
      case 3:
        return !!requestState.get('workspace');
      default:
        return false;
    }
  },
);

export const getCurrentPage = () => createSelector(
  selectors.requestSelector,
  (requestState) => requestState.get('currentPage'),
);

