import { fromJS } from "immutable";
import * as Fuse from 'fuse.js';

const initialState = fromJS({
  fetching: false,
  allWorkspaces: new Fuse([], { keys: ['name'] }),
  filter: '',
  behavior: '',
});

const listing = (state = initialState, action: any) => {
  switch (action.type) {
    case 'SET_WORKSPACE_LIST':
      return state
        .set('fetching', false)
        .set('allWorkspaces', action.workspaceList)
    case 'LIST_WORKSPACES':
      return state
        .set('fetching', true)
    default:
      return state;
  }
}

export default listing;