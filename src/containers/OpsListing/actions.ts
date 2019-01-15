import { WorkspaceSearchResult } from '../../models/Workspace';

export const LIST_OPS_WORKSPACES = 'LIST_OPS_WORKSPACES';
export const listOpsWorkspaces = () => ({
  type: LIST_OPS_WORKSPACES,
});

export const OPS_WORKSPACES_UPDATED = 'OPS_WORKSPACES_UPDATED';
export const opsWorkspacesUpdated = (workspaces: WorkspaceSearchResult[]) => ({
  type: OPS_WORKSPACES_UPDATED,
  workspaces,
});

export const LIST_OPS_WORKSPACES_FAILURE = 'LIST_OPS_WORKSPACES_FAILURE';
export const listOpsWorkspacesFailure = (error: string) => ({
  type: LIST_OPS_WORKSPACES_FAILURE,
  error,
});

export const SET_LISTING_MODE = 'SET_LISTING_MODE';
export const setListingMode = (mode: string) => ({
  type: SET_LISTING_MODE,
  mode,
});
