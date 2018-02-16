export const SHARED_WORKSPACE_DETAILS = "SHARED_WORKSPACE_DETAILS";

export function sharedWorkspaceDetails (workspace) {
    return {
        type: SHARED_WORKSPACE_DETAILS,
        workspace
    }
}