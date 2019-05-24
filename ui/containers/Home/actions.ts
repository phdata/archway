import { Workspace } from '../../models/Workspace';

export const SET_RECENT_WORKSPACES = 'SET_RECENT_WORKSPACES';
export const setRecentWorkspaces = (workspaces: Workspace[]) => ({
  type: SET_RECENT_WORKSPACES,
  workspaces,
});

export const REFRESH_RECENT_WORKSPACES = 'REFRESH_RECENT_WORKSPACES';
export const refreshRecentWorkspaces = () => ({
  type: REFRESH_RECENT_WORKSPACES,
});
