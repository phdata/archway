import { fromJS } from "immutable";
import * as Fuse from 'fuse.js';

const initialState = fromJS({
  fetching: false,
  allWorkspaces: new Fuse([], {}),
  filters: {
    filter: '',
    behaviors: ['simple', 'structured'],
  }
});

const listing = (state = initialState, action: any) => {
  switch (action.type) {

    case 'WORKSPACE_LISTING_UPDATED':
      return state
        .set('fetching', false)
        .set('allWorkspaces', new Fuse(action.workspaces, {}));

    case 'LIST_ALL_WORKSPACES':
      return state
        .set('fetching', true);

    case 'FILTER_WORKSPACES':
      return state
        .set('filters', fromJS(action.filters));

    default:
      return state;

  }
}

export default listing;