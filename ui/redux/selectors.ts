import { createSelector, ParametricSelector } from 'reselect';
import { Profile } from '../models/Profile';
import { Filters } from '../models/Listing';
import { WorkspaceSearchResult } from '../models/Workspace';

export const authSelector = (state: any) => state.get('login');
export const clusterSelector = (state: any) => state.get('cluster');
export const requestSelector = (state: any) => state.get('request');
export const workspaceSelector = (state: any) => state.get('details');
export const workspaceListSelector = (state: any) => state.get('listing');
export const riskSelector = (state: any) => state.get('risk');
export const opsSelector = (state: any) => state.get('operations');
export const manageSelector = (state: any) => state.get('manage');
export const homeSelector = (state: any) => state.get('home');
export const templatesSelector = (state: any) => state.get('templates');
export const configSelector = (state: any) => state.get('config');
export const formSelector = (state: any) => state.get('form');

export const isLoading = () =>
  createSelector(
    authSelector,
    authState => authState.get('loading')
  );

export const getToken = () =>
  createSelector(
    authSelector,
    authState => authState.get('token')
  );

export const getProfile = () =>
  createSelector(
    authSelector,
    authState => authState.get('profile') as Profile
  );

export const getFeatureFlags = () =>
  createSelector(
    configSelector,
    configState => configState.get('featureFlags').toJS()
  );

export class SearchBarSelector {
  public parentSelector: ParametricSelector<any, any, any>;

  constructor(parentSelector: ParametricSelector<any, any, any>) {
    this.parentSelector = parentSelector;
  }

  public fuseList = () =>
    createSelector(
      this.parentSelector,
      listingState => listingState.get('workspaces')
    );

  public getListingMode = () =>
    createSelector(
      this.parentSelector,
      listingState => listingState.get('listingMode')
    );

  public getListFilters = () =>
    createSelector(
      this.parentSelector,
      listingState => listingState.get('filters').toJS()
    );

  public workspaceList = () =>
    createSelector(
      this.fuseList(),
      this.getListFilters(),
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
          .filter(
            (workspace: WorkspaceSearchResult) => filters.statuses.indexOf((workspace.status || '').toLowerCase()) >= 0
          );
      }
    );

  public isFetchingWorkspaces = () =>
    createSelector(
      this.parentSelector,
      listingState => listingState.get('fetching')
    );
}
