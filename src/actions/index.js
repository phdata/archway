export const LOGIN_REQUEST = 'LOGIN_REQUEST';
export const LOGIN_SUCCESS = 'LOGIN_SUCCESS';
export const LOGIN_FAILURE = 'LOGIN_FAILURE';

export const LOGOUT_REQUEST = 'LOGOUT_REQUEST';
export const LOGOUT_SUCCESS = 'LOGOUT_SUCCESS';
export const LOGOUT_FAILURE = 'LOGOUT_FAILURE';

export const PROFILE_READY = 'PROFILE_READY';

export const CLUSTER_INFO = "CLUSTER_INFO";

export const TOKEN_EXTRACTED = 'TOKEN_EXTRACTED';
export const TOKEN_NOT_AVAILABLE = 'TOKEN_NOT_AVAILABLE';

export const WORKSPACE_AVAILABLE = "WORKSPACE_AVAILABLE";
export const WORKSPACE_ABSENT = "WORKSPACE_ABSENT";
export const WORKSPACE_REQUESTED = "WORKSPACE_REQUESTED";

export const SHARED_WORKSPACES_REQUESTED = "SHARED_WORKSPACES_REQUESTED";
export const SHARED_WORKSPACES_SUCCESS = "SHARED_WORKSPACES_SUCCESS";
export const SHARED_WORKSPACES_FAILED = "SHARED_WORKSPACES_FAILED";

export const REQUEST_SHARED_WORKSPACE = "REQUEST_SHARED_WORKSPACE";
export const REQUEST_SHARED_WORKSPACE_SUCCESS = "REQUEST_SHARED_WORKSPACE_SUCCESS";
export const REQUEST_SHARED_WORKSPACE_FAILED = "REQUEST_SHARED_WORKSPACE_FAILED";

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

export function tokenNotAvailalbe() {
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

export function workspaceAbsent() {
    return {
        type: WORKSPACE_ABSENT
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

export function profileReady(profile) {
    return {
        type: PROFILE_READY,
        profile
    };
}

export function workspacesRequested() {
    return {
        type: SHARED_WORKSPACES_REQUESTED
    };
}

export function workspacesSuccess(workspaces) {
    return {
        type: SHARED_WORKSPACES_SUCCESS,
        items: workspaces
    };
}

export function workspacesFailed(error) {
    return {
        type: SHARED_WORKSPACES_FAILED,
        error
    };
}

export function requestSharedWorkspace({name, purpose, phi_data, pii_data, pci_data}) {
    return {
        type: REQUEST_SHARED_WORKSPACE,
        request: {
            name,
            purpose,
            compliance: {
                pii_data,
                pci_data,
                phi_data
            },
            hdfs: {
                requested_size_in_gb: 12
            },
            yarn: {
                max_cores: 10,
                max_memory_in_gb: 8
            }
        }
    }
}