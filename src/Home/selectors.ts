import { createSelector } from 'reselect';
import { getCluster } from '../selectors';
import { Cluster } from './a.d';

export const getClusterInfo = () => createSelector(
  getCluster(),
  clusterState => clusterState.get('details').toJS() as Cluster
);