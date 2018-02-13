export const WORKSPACE_AVAILABLE = "WORKSPACE_AVAILABLE";
export const WORKSPACE_ABSENT = "WORKSPACE_ABSENT";
export const WORKSPACE_REQUESTED = "WORKSPACE_REQUESTED";

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