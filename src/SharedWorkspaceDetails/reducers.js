import {SHARED_WORKSPACE_DETAILS} from "./actions";

const initialState = {};

function sharedWorkspaceDetails(state = initialState, action) {
    switch(action.type) {
        case SHARED_WORKSPACE_DETAILS:
            return action.workspace;
        default:
            return state;
    }
}

export default sharedWorkspaceDetails;