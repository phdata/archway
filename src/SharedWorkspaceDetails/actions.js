export const SHARED_WORKSPACE_DETAILS = "SHARED_WORKSPACE_DETAILS";

export function sharedWorkspaceDetails(workspace) {
    return {
        type: SHARED_WORKSPACE_DETAILS,
        workspace
    }
}

export const WORKSPACE_MEMBER_LIST = "WORKSPACE_MEMBER_LIST";

export function workspaceMemberList(members) {
    return {
        type: WORKSPACE_MEMBER_LIST,
        members
    }
}