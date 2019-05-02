import { WorkspaceSearchResult, Workspace } from '../../models/Workspace';

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

export const WORKSAPCE_VIEWED = 'WORKSAPCE_VIEWED';
export const workspaceViewed = (workspace: Workspace) => ({
  type: WORKSAPCE_VIEWED,
  workspace,
});

export const SET_RECENT_WORKSPACES = 'SET_RECENT_WORKSPACES';
export const setRecentWorkspaces = (workspaces: Workspace[]) => ({
  type: SET_RECENT_WORKSPACES,
  workspaces,
});

export const REFRESH_RECENT_WORKSPACES = 'REFRESH_RECENT_WORKSPACES';
export const refreshRecentWorkspaces = () => ({
  type: REFRESH_RECENT_WORKSPACES,
});
