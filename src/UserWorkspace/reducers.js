import {
    WORKSPACE_ABSENT,
    WORKSPACE_AVAILABLE,
    WORKSPACE_REQUESTED
} from './actions'

const initialState = {
    error: null,
    loading: true
};

function userWorkspace(state = initialState, action ) {
    switch (action.type) {
        case WORKSPACE_AVAILABLE:
            return {
                ...state,
                requesting: false,
                workspace: action.workspace
            };
        case WORKSPACE_ABSENT:
            return {
                ...state,
                loading: false,
                workspace: null
            };
        case WORKSPACE_REQUESTED:
            return {
                ...state,
                requesting: true
            };
        default:
            return state
    }
}

export default userWorkspace;