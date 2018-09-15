export const CLUSTER_INFO = "CLUSTER_INFO";

export function clusterInfo(cluster) {
    return {
        type: CLUSTER_INFO,
        cluster
    };
}