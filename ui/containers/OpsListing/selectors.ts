import { createSelector } from 'reselect';
import { opsSelector } from '../../redux/selectors';
import { WorkspaceSearchResult } from '../../models/Workspace';
import { Filters } from '../../models/Listing';

export const fuseList = () =>
  createSelector(
    opsSelector,
    opsState => opsState.get('workspaces')
  );

export const getListFilters = () =>
  createSelector(
    opsSelector,
    opsState => opsState.get('filters').toJS()
  );

export const opsWorkspaceList = () =>
  createSelector(
    fuseList(),
    getListFilters(),
    (fuse, filters: Filters) => {
      return (filters.filter ? fuse.search(filters.filter) : fuse.list)
        .filter((workspace: WorkspaceSearchResult) => {
          const behavior = workspace.behavior.toLowerCase();
          let isCustomWorkspace: boolean = false;
          if (filters.behaviors.includes('custom')) {
            isCustomWorkspace = behavior !== '' && behavior !== 'simple' && behavior !== 'structured';
          }
          return filters.behaviors.includes(workspace.behavior.toLowerCase()) || isCustomWorkspace;
        })
        .filter((workspace: WorkspaceSearchResult) => filters.statuses.includes(workspace.status.toLowerCase()));
    }
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
