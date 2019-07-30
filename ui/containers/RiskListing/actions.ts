import { WorkspaceSearchResult } from '../../models/Workspace';

export const LIST_RISK_WORKSPACES = 'LIST_RISK_WORKSPACES';
export const listRiskWorkspaces = () => ({
  type: LIST_RISK_WORKSPACES,
});

export const RISK_WORKSPACES_UPDATED = 'RISK_WORKSPACES_UPDATED';
export const riskWorkspacesUpdated = (workspaces: WorkspaceSearchResult[]) => ({
  type: RISK_WORKSPACES_UPDATED,
  workspaces,
});

export const LIST_RISK_WORKSPACES_FAILURE = 'LIST_RISK_WORKSPACES_FAILURE';
export const listRiskWorkspacesFailure = (error: string) => ({
  type: LIST_RISK_WORKSPACES_FAILURE,
  error,
});

export const SET_RISK_LISTING_MODE = 'SET_RISK_LISTING_MODE';
export const setListingMode = (mode: string) => ({
  type: SET_RISK_LISTING_MODE,
  mode,
});

export const FILTER_RISK_WORKSPACES = 'FILTER_RISK_WORKSPACES';
export const filterWorkspaces = (filter: string, behaviors: string[], statuses: string[]) => ({
  type: FILTER_RISK_WORKSPACES,
  filters: {
    filter,
    behaviors,
    statuses,
  },
});
