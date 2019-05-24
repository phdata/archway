import Fuse from 'fuse.js';
import { fromJS } from 'immutable';
import {
  FILTER_WORKSPACES,
  LIST_ALL_WORKSPACES,
  WORKSPACE_LISTING_UPDATED,
  SET_LISTING_MODE,
} from './actions';


const initialState = fromJS({
  listingMode: localStorage.getItem('workspaceListingMode') || 'cards',
  fetching: false,
  allWorkspaces: new Fuse([], {}),
  filters: {
    filter: '',
    behaviors: ['simple', 'structured'],
    statuses: ['approved', 'pending', 'rejected'],
  },
});

const listing = (state = initialState, action: any) => {
  switch (action.type) {

    case WORKSPACE_LISTING_UPDATED:
      return state
        .set('fetching', false)
        .set('allWorkspaces', new Fuse(action.workspaces, { keys: ['name', 'summary'] }));

    case LIST_ALL_WORKSPACES:
      return state
        .set('allWorkspaces', new Fuse([], {}))
        .set('fetching', true);

    case SET_LISTING_MODE:
      localStorage.setItem('workspaceListingMode', action.mode);
      return state
        .set('listingMode', action.mode);

    case FILTER_WORKSPACES:
      return state
        .set('filters', fromJS(action.filters));

    default:
      return state;

  }
};

export default listing;
