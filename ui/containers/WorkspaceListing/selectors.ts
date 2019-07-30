import { createSelector } from 'reselect';
import { workspaceListSelector, authSelector, clusterSelector } from '../../redux/selectors';
import { Profile } from '../../models/Profile';
import { Cluster } from '../../models/Cluster';
import { SearchBarSelector } from '../../redux/selectors';

export const SearchBar = new SearchBarSelector(workspaceListSelector);

export const getProfile = () =>
  createSelector(
    authSelector,
    authState => authState.get('profile') as Profile
  );

export const getCluster = () =>
  createSelector(
    clusterSelector,
    clusterState => clusterState.get('details').toJS() as Cluster
  );
