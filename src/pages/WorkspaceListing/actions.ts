import { Workspace } from '../../types/Workspace';

export const FILTER_WORKSPACES = 'FILTER_WORKSPACES';
export const filterWorkspaces = (filter: string, behaviors: string[]) => ({
  type: FILTER_WORKSPACES,
  filters: {
    filter,
    behaviors,
  },
});

export const LIST_ALL_WORKSPACES = 'LIST_ALL_WORKSPACES';
export const listAllWorkspaces = () => ({
  type: LIST_ALL_WORKSPACES,
});

export const WORKSPACE_LISTING_UPDATED = 'WORKSPACE_LISTING_UPDATED';
export const workspaceListUpdated = (workspaces: Workspace[]) => ({
  type: WORKSPACE_LISTING_UPDATED,
  workspaces,
});
