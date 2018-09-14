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
    if(filters.filter === '') {
      return fuse.list;
    } else {
      return fuse.search(filters.filter).filter((workspace: Workspace) => filters.behaviors.indexOf(workspace.behavior) >= 0);
    }
  }
);

export const isFetchingWorkspaces = () => createSelector(
  workspaceListSelector,
  listingState => listingState.get('fetching')
);