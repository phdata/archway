import { createSelector } from 'reselect';
import { riskSelector } from '../../redux/selectors';
import { WorkspaceSearchResult } from '../../models/Workspace';

export const riskWorkspaceList = () =>
  createSelector(
    riskSelector,
    riskState => riskState.get('workspaces') as WorkspaceSearchResult[]
  );

export const getListingMode = () =>
  createSelector(
    riskSelector,
    riskState => riskState.get('listingMode')
  );

export const isFetchingRiskWorkspaces = () =>
  createSelector(
    riskSelector,
    riskState => riskState.get('fetching')
  );
