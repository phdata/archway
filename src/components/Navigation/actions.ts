import { Cluster } from '../../types/Cluster';

export const CLUSTER_INFO = 'CLUSTER_INFO';

export function clusterInfo(cluster: Cluster) {
  return {
      type: CLUSTER_INFO,
      cluster,
    };
}
