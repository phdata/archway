export const SET_REQUEST_TYPE = 'SET_REQUEST_TYPE';

export function setRequestType(type) {
  return {
    type: SET_REQUEST_TYPE,
    requestType: type,
  };
}

export const WORKSPACE_GENERATED = 'WORKSPACE_GENERATED';

export function workspaceGenerated(workspace) {
  return {
    type: WORKSPACE_GENERATED,
    workspace,
  };
}

export const REQUEST_CHANGED = 'REQUEST_CHANGED';

export function requestChanged(field) {
  return {
    type: REQUEST_CHANGED,
    field,
  };
}

export const SET_REQUEST = 'SET_REQUEST';

export function setRequest(request) {
  return {
    type: SET_REQUEST,
    request,
  };
}

export const SET_GENERATING = 'SET_GENERATING';

export function setGenerating(generating) {
  return {
    type: SET_GENERATING,
    generating,
  };
}

export const WORKSPACE_REQUESTED = 'WORKSPACE_REQUESTED';

export function workspaceRequested() {
  return {
    type: WORKSPACE_REQUESTED,
  };
}
