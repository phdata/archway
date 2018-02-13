import {
    REQUEST_SHARED_WORKSPACE,
    SHARED_WORKSPACES_FAILED,
    SHARED_WORKSPACES_REQUESTED,
    SHARED_WORKSPACES_SUCCESS
} from './actions';

const initialState = {
    items: false,
    loading: true,
    error: false
};

function workspaces(state = initialState, action) {
    switch (action.type) {
        case SHARED_WORKSPACES_REQUESTED:
            return {
                ...state,
                loading: true
            };
        case SHARED_WORKSPACES_SUCCESS:
            return {
                ...state,
                loading: false,
                items: action.items,
                error: false
            };
        case SHARED_WORKSPACES_FAILED:
            return {
                ...state,
                loading: false,
                items: false,
                error: action.error
            };
        case REQUEST_SHARED_WORKSPACE:
            return {
                ...state,
                loading: true
            };
        default:
            return state
    }
}

export default workspaces;