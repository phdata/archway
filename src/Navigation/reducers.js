import {CLUSTER_INFO} from "./actions";

const initialState = {
    name: "Unknown",
    status: "unknown",
    color: "#F0F3F5",
    displayStatus: "unknown",
};

function cluster(state = initialState, action ) {
    switch (action.type) {
        case CLUSTER_INFO:
          const cluster = action.cluster[0];
          switch (cluster.status) {
            case 'GOOD_HEALTH':
              return {
                ...cluster,
                color: '#43AA8B',
                displayStatus: 'swell',
              };
              break;
            case 'CONCERNING_HEALTH':
              return {
                ...cluster,
                color: '#FF6F59',
                displayStatus: 'iffy',
              };
              break;
            case 'BAD_HEALTH':
              return {
                ...cluster,
                color: '#DB504A',
                displayStatus: 'not good',
              };
              break;
            default:
              return {
                color: '#aaa',
                displayStatus: 'unknown',
              };
              break;
          }
        default:
            return state
    }
}

export default cluster;
