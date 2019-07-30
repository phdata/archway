import Fuse from 'fuse.js';
import { fromJS } from 'immutable';
import { FILTER_WORKSPACES, LIST_ALL_WORKSPACES, WORKSPACE_LISTING_UPDATED, SET_LISTING_MODE } from './actions';
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
});

const listing = (state = initialState, action: any) => {
  switch (action.type) {
    case WORKSPACE_LISTING_UPDATED:
      return state
        .set('fetching', false)
        .set('workspaces', new Fuse(action.workspaces, { keys: ['name', 'summary'], threshold: 0.2 }));

    case LIST_ALL_WORKSPACES:
      return state.set('workspaces', new Fuse([], {})).set('fetching', true);

    case SET_LISTING_MODE:
      localStorage.setItem('workspaceListingMode', action.mode);
      return state.set('listingMode', action.mode);

    case FILTER_WORKSPACES:
      return state.set('filters', fromJS(action.filters));

    default:
      return state;
  }
};

export default listing;
