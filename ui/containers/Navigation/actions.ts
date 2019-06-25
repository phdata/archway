import { Cluster } from '../../models/Cluster';

export const CLUSTER_INFO = 'CLUSTER_INFO';
export const CLUSTER_LOADING = 'CLUSTER_LOADING';

export function clusterInfo(cluster: Cluster) {
  return {
    type: CLUSTER_INFO,
    cluster,
  };
}

export function clusterLoading(loading: boolean) {
  return {
    type: CLUSTER_LOADING,
    loading,
  };
}
