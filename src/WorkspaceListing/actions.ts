import { Workspace } from "./Workspace";

export const filterWorkspaces = 
  (filter: string, behaviors: string[]) => ({
    type: 'FILTER_WORKSPACES',
    filters: {
      filter,
      behaviors,
    }
});

export const listAllWorkspaces = () => ({
  type: 'LIST_ALL_WORKSPACES'
});

export const workspaceListUpdated = (workspaces: Workspace[]) => ({
  type: 'WORKSPACE_LISTING_UPDATED',
  workspaces
});