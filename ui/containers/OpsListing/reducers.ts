import { fromJS } from 'immutable';
import {
  LIST_OPS_WORKSPACES,
  OPS_WORKSPACES_UPDATED,
  LIST_OPS_WORKSPACES_FAILURE,
  SET_LISTING_MODE,
} from './actions';

const initialState = fromJS({
  listingMode: localStorage.getItem('workspaceListingMode') || 'cards',
  fetching: false,
  workspaces: [],
  error: '',
});

const listing = (state = initialState, action: any) => {
  switch (action.type) {

    case OPS_WORKSPACES_UPDATED:
      return state
        .set('fetching', false)
        .set('error', '')
        .set('workspaces', action.workspaces);

    case LIST_OPS_WORKSPACES_FAILURE:
      return state
        .set('fetching', false)
        .set('error', action.error);

    case LIST_OPS_WORKSPACES:
      return state
        .set('workspaces', [])
        .set('error', '')
        .set('fetching', true);

    case SET_LISTING_MODE:
      localStorage.setItem('workspaceListingMode', action.mode);
      return state
        .set('listingMode', action.mode);

    default:
      return state;

  }
};

export default listing;