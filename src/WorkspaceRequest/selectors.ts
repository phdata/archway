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
  requestState => requestState.get('workspace') as Workspace
);