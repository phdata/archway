import { createSelector } from 'reselect';
import * as selectors from '../../redux/selectors';
import { RequestInput } from '../../models/RequestInput';
import { Workspace } from '../../models/Workspace';
import { Profile } from '../../models/Profile';
import {
  PAGE_BEHAVIOR,
  PAGE_DETAILS,
  PAGE_COMPLIANCE,
  PAGE_REVIEW,
} from './constants';

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

export const getError = () => createSelector(
  selectors.requestSelector,
  (requestState) => requestState.get('error'),
);

export const isNextStepEnabled = () => createSelector(
  selectors.requestSelector,
  (requestState) => {
    if (requestState.get('loading')) {
      return false;
    }

    switch (requestState.get('currentPage')) {
      case PAGE_BEHAVIOR:
        return !!requestState.get('template');
      case PAGE_DETAILS:
      {
        const request: RequestInput = requestState.get('request').toJS();
        return !!request.name && !!request.summary && !!request.description;
      }
      case PAGE_COMPLIANCE:
      {
        const request: RequestInput = requestState.get('request').toJS();
        return (request.compliance.pci_data !== undefined)
          && (request.compliance.pii_data !== undefined)
          && (request.compliance.phi_data !== undefined);
      }
      case PAGE_REVIEW:
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

export const isAdvancedVisible = () => createSelector(
  selectors.requestSelector,
  (requestState) => requestState.get('advancedVisible'),
);
