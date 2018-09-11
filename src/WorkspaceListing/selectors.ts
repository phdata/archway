import { createSelector } from 'reselect';

export const workspaceListSelector = (state: any) => state.get('workspaceList');

const fuseList = () => createSelector(
  workspaceListSelector,
  listingState => listingState.get('allWorkspaces')
)

const filter = () => createSelector(
  workspaceListSelector,
  listingState => listingState.get('filter')
)

const behavior = () => createSelector(
  workspaceListSelector,
  listingState => listingState.get('behavior')
)

export const workspaceList = () => createSelector(
  fuseList(),
  filter(),
  behavior(),
  (fuse, filter: string, behavior: string) => fuse.search(filter) /* .map(i => i.behavior === behavior) */
);

export const isFetchingWorkspaces = () => createSelector(
  workspaceListSelector,
  listingState => listingState.get('fetching')
);