import { createSelector } from 'reselect';
import { riskSelector } from '../../redux/selectors';
import { WorkspaceSearchResult } from '../../models/Workspace';
import { Filters } from '../../models/Listing';

export const fuseList = () =>
  createSelector(
    riskSelector,
    riskState => riskState.get('workspaces')
  );

export const getListFilters = () =>
  createSelector(
    riskSelector,
    riskState => riskState.get('filters').toJS()
  );

export const riskWorkspaceList = () =>
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
    riskSelector,
    riskState => riskState.get('listingMode')
  );

export const isFetchingRiskWorkspaces = () =>
  createSelector(
    riskSelector,
    riskState => riskState.get('fetching')
  );
