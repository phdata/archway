import { createSelector } from 'reselect';
import * as selectors from '../selectors';
import {RequestInput} from './model';
import { Workspace } from '../WorkspaceListing/Workspace';

export const getBehavior = () => createSelector(
  selectors.getRequest(),
  requestState => requestState.get('behavior')
);

export const getRequest = () => createSelector(
  selectors.getRequest(),
  requestState => requestState.get('request') && requestState.get('request') as RequestInput
);

export const getSelectedPage = () => createSelector(
  selectors.getRequest(),
  requestState => requestState.get('page')
);

export const getGeneratedWorkspace = () => createSelector(
  selectors.getRequest(),
  requestState => requestState.get('workspace') && requestState.get('workspace').toJS() as Workspace
);

export const isReady = () => createSelector(
  selectors.getRequest(),
  requestState => {
    switch(requestState.get('page')) {
      case 1:
        return !!requestState.get('behavior');
      case 2:
        return !!requestState.get('request');
      default:
        return false;
    }
  }
);

export const isCompleteEnabled = () => createSelector(
  selectors.getRequest(),
  requestState => 
    requestState.get('page') === 3 && !!requestState.get('behavior') && !!requestState.get('request')
);