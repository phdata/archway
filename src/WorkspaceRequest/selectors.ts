import { createSelector } from 'reselect';
import * as selectors from '../selectors';

export const getBehavior = () => createSelector(
  selectors.getRequest(),
  requestState => {
    console.log('behavior');
    return requestState.get('behavior')
  }
);

export const getRequest = () => createSelector(
  selectors.getRequest(),
  requestState => requestState.get('request').toJS() as Request
);

export const getSelectedPage = () => createSelector(
  selectors.getRequest(),
  requestState => requestState.get('selectedPage')
);