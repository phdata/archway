import Fuse from 'fuse.js';
import { fromJS } from 'immutable';
import {
  LIST_OPS_WORKSPACES,
  OPS_WORKSPACES_UPDATED,
  LIST_OPS_WORKSPACES_FAILURE,
  SET_RISK_LISTING_MODE,
  FILTER_OPS_WORKSPACES,
} from './actions';
import { workspaceStatuses, workspaceBehaviors } from '../../constants';

const initialState = fromJS({
  listingMode: localStorage.getItem('workspaceListingMode') || 'cards',
  fetching: false,
  workspaces: new Fuse([], {}),
  filters: {
    filter: '',
    behaviors: workspaceBehaviors,
    statuses: workspaceStatuses,
  },
  error: '',
});

const listing = (state = initialState, action: any) => {
  switch (action.type) {
    case OPS_WORKSPACES_UPDATED:
      return state
        .set('fetching', false)
        .set('error', '')
        .set('workspaces', new Fuse(action.workspaces, { keys: ['name', 'summary'], threshold: 0.2 }));

    case LIST_OPS_WORKSPACES_FAILURE:
      return state.set('fetching', false).set('error', action.error);

    case LIST_OPS_WORKSPACES:
      return state
        .set('workspaces', new Fuse([], {}))
        .set('error', '')
        .set('fetching', true);

    case SET_RISK_LISTING_MODE:
      localStorage.setItem('workspaceListingMode', action.mode);
      return state.set('listingMode', action.mode);

    case FILTER_OPS_WORKSPACES:
      return state.set('filters', fromJS(action.filters));

    default:
      return state;
  }
};

export default listing;
