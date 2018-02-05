export const LOGIN_REQUEST = 'LOGIN_REQUEST';
export const LOGIN_SUCCESS = 'LOGIN_SUCCESS';
export const LOGIN_FAILURE = 'LOGIN_FAILURE';

export const LOGOUT_REQUEST = 'LOGOUT_REQUEST';
export const LOGOUT_SUCCESS = 'LOGOUT_SUCCESS';
export const LOGOUT_FAILURE = 'LOGOUT_FAILURE';

export const CLUSTER_INFO = "CLUSTER_INFO";

export const TOKEN_EXTRACTED = 'TOKEN_EXTRACTED';
export const TOKEN_NOT_AVAILABLE = 'TOKEN_NOT_AVAILABLE';

export const WORKSPACE_AVAILABLE = "WORKSPACE_AVAILABLE";
export const WORKSPACE_REQUESTED = "WORKSPACE_REQUESTED";

export function login({username, password}) {
    return {
        type: LOGIN_REQUEST,
        username,
        password
    };
}

export function loginSuccess(token) {
    return {
        type: LOGIN_SUCCESS,
        token
    };
}

export function loginError(error) {
    return {
        type: LOGIN_FAILURE,
        error
    };
}

export function tokenExtracted(token) {
    return {
        type: TOKEN_EXTRACTED,
        token
    };
}

export function tokenNotAvailable() {
    return {
        type: TOKEN_NOT_AVAILABLE
    };
}

export function workspaceFound(workspace) {
    return {
        type: WORKSPACE_AVAILABLE,
        workspace
    };
}

export function requestWorkspace() {
    return {
        type: WORKSPACE_REQUESTED
    };
}

export function clusterInfo(cluster) {
    return {
        type: CLUSTER_INFO,
        cluster
    };
}