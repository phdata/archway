import { WorkspaceSearchResult } from '../../models/Workspace';

export const FILTER_WORKSPACES = 'FILTER_WORKSPACES';
export const filterWorkspaces = (filter: string, behaviors: string[], statuses: string[]) => ({
  type: FILTER_WORKSPACES,
  filters: {
    filter,
    behaviors,
    statuses,
  },
});

export const SET_LISTING_MODE = 'SET_LISTING_MODE';
export const setListingMode = (mode: string) => ({
  type: SET_LISTING_MODE,
  mode,
});

export const LIST_ALL_WORKSPACES = 'LIST_ALL_WORKSPACES';
export const listAllWorkspaces = () => ({
  type: LIST_ALL_WORKSPACES,
});

export const WORKSPACE_LISTING_UPDATED = 'WORKSPACE_LISTING_UPDATED';
export const workspaceListUpdated = (workspaces: WorkspaceSearchResult[]) => ({
  type: WORKSPACE_LISTING_UPDATED,
  workspaces,
});