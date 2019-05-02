import * as Fuse from 'fuse.js';
import { fromJS } from 'immutable';
import {
  FILTER_WORKSPACES,
  LIST_ALL_WORKSPACES,
  WORKSPACE_LISTING_UPDATED,
  WORKSAPCE_VIEWED,
  SET_RECENT_WORKSPACES,
  SET_LISTING_MODE,
} from './actions';
import { Workspace } from '../../models/Workspace';

const recentWorkspacesKey = 'recentWorkspaces';

const initialState = fromJS({
  listingMode: localStorage.getItem('workspaceListingMode') || 'cards',
  fetching: false,
  allWorkspaces: new Fuse([], {}),
  filters: {
    filter: '',
    behaviors: ['simple', 'structured'],
    statuses: ['approved', 'pending', 'rejected'],
  },
  recent: JSON.parse(localStorage.getItem(recentWorkspacesKey) || '[]'),
});

const listing = (state = initialState, action: any) => {
  switch (action.type) {

    case WORKSPACE_LISTING_UPDATED:
      return state
        .set('fetching', false)
        .set('allWorkspaces', new Fuse(action.workspaces, { keys: ['name', 'summary'] }));

    case WORKSAPCE_VIEWED:
      {
        const recentWorkspaces = [
          action.workspace,
          ...state.get('recent').toJS().filter((w: Workspace) => w.id !== action.workspace.id),
        ].slice(0, 2);
        localStorage.setItem(recentWorkspacesKey, JSON.stringify(recentWorkspaces));

        return state
          .set('recent', fromJS(recentWorkspaces));
      }

    case SET_RECENT_WORKSPACES:
      return state
        .set('recent', fromJS(action.workspaces));

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
