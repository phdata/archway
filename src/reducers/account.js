import {
    LOGIN_FAILURE,
    LOGIN_REQUEST,
    LOGIN_SUCCESS,
    TOKEN_EXTRACTED,
    TOKEN_NOT_AVAILABLE, WORKSPACE_ABSENT,
    WORKSPACE_AVAILABLE,
    WORKSPACE_REQUESTED
} from '../actions'

const initialState = {
    token: null,
    error: null,
    loggingIn: false,
    loading: true
};

function token(state = initialState, action ) {
    console.log(action);
    switch (action.type) {
        case LOGIN_REQUEST:
            return {
                ...state,
                loggingIn: true
            };
        case LOGIN_SUCCESS:
            return {
                ...state,
                loggingIn: false,
                token: action.token
            };
        case LOGIN_FAILURE:
            return {
                ...state,
                loggingIn: false,
                error: action.error
            };
        case TOKEN_EXTRACTED:
            return {
                ...state,
                token: action.token
            };
        case WORKSPACE_AVAILABLE:
            return {
                ...state,
                loading: false,
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

export default token;