export const REQUEST_SHARED_WORKSPACE = "REQUEST_SHARED_WORKSPACE";

export const SHARED_WORKSPACES_REQUESTED = "SHARED_WORKSPACES_REQUESTED";
export const SHARED_WORKSPACES_SUCCESS = "SHARED_WORKSPACES_SUCCESS";
export const SHARED_WORKSPACES_FAILED = "SHARED_WORKSPACES_FAILED";

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