import * as Fuse from 'fuse.js';
import { fromJS } from 'immutable';
import { FILTER_WORKSPACES, LIST_ALL_WORKSPACES, WORKSPACE_LISTING_UPDATED } from './actions';

const initialState = fromJS({
  fetching: false,
  allWorkspaces: new Fuse([], {}),
  filters: {
    filter: '',
    behaviors: ['simple', 'structured'],
  },
});

const listing = (state = initialState, action: any) => {
  switch (action.type) {

    case WORKSPACE_LISTING_UPDATED:
      return state
        .set('fetching', false)
        .set('allWorkspaces', new Fuse(action.workspaces, { keys: ['name', 'summary', 'description'] }));

    case LIST_ALL_WORKSPACES:
      return state
        .set('allWorkspaces', new Fuse([], {}))
        .set('fetching', true);

    case FILTER_WORKSPACES:
      return state
        .set('filters', fromJS(action.filters));

    default:
      return state;

  }
};

export default listing;
