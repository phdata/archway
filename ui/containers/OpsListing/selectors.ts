import { createSelector } from 'reselect';
import { opsSelector } from '../../redux/selectors';
import { WorkspaceSearchResult } from '../../models/Workspace';

export const opsWorkspaceList = () =>
  createSelector(
    opsSelector,
    opsState => opsState.get('workspaces') as WorkspaceSearchResult[]
  );

export const getListingMode = () =>
  createSelector(
    opsSelector,
    opsState => opsState.get('listingMode')
  );

export const isFetchingOpsWorkspaces = () =>
  createSelector(
    opsSelector,
    opsState => opsState.get('fetching')
  );
