import {CLUSTER_INFO} from "./actions";

const initialState = {
    name: "Unknown",
    status: "unknown"
};

function cluster(state = initialState, action ) {
    switch (action.type) {
        case CLUSTER_INFO:
            return action.cluster[0];
        default:
            return state
    }
}

export default cluster;