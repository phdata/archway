import { createSelector } from 'reselect';
import { Workspace } from './Workspace';

export const workspaceListSelector = (state: any) => state.get('workspaceList');

const fuseList = () => createSelector(
  workspaceListSelector,
  listingState => listingState.get('allWorkspaces')
)

export const getListFilters = () => createSelector(
  workspaceListSelector,
  listingState => listingState.get('filters').toJS()
)

export const workspaceList = () => createSelector(
  fuseList(),
  getListFilters(),
  (fuse, filters: {filter: string, behaviors: string[]}) => {
    console.log(filters);
    return fuse.search(filters.filter).filter((workspace: Workspace) => filters.behaviors.indexOf(workspace.behavior))
  }
);

export const isFetchingWorkspaces = () => createSelector(
  workspaceListSelector,
  listingState => listingState.get('fetching')
);