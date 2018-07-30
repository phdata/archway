export const CHANGE_ACTIVE_WORKSPACE = 'CHANGE_ACTIVE_WORKSPACE';

export function changeActiveWorkspace(workspace) {
  return {
    type: CHANGE_ACTIVE_WORKSPACE,
    workspace,
  };
}

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


export const REQUEST_WORKSPACE = 'REQUEST_WORKSPACE';

export function requestWorkspace() {
  return {
    type: REQUEST_WORKSPACE,
  };
}

export const SET_WORKSPACE = 'SET_WORKSPACE';

export function setWorkspace(workspace) {
  return {
    type: SET_WORKSPACE,
    workspace,
  };
}

export const WORKSPACE_REQUESTED = 'WORKSPACE_REQUESTED';

export function workspaceRequested() {
  return {
    type: WORKSPACE_REQUESTED,
  };
}

export const LIST_WORKSPACES = 'LIST_WORKSPACES';

export function listWorkspaces() {
  return {
    type: LIST_WORKSPACES,
  };
}

export const SET_WORKSPACE_LIST = 'SET_WORKSPACE_LIST';

export function setWorkspaceList(workspaceList) {
  return {
    type: SET_WORKSPACE_LIST,
    workspaceList,
  };
}

export const GET_WORKSPACE = 'GET_WORKSPACE';

export function getWorkspace(id) {
  return {
    type: GET_WORKSPACE,
    id,
  };
}

export const APPROVE_WORKSPACE_REQUESTED = 'APPROVE_WORKSPACE_REQUESTED';

export function approveInfra() {
  return {
    type: APPROVE_WORKSPACE_REQUESTED,
    role: 'infra',
  };
}

export function approveRisk() {
  return {
    type: APPROVE_WORKSPACE_REQUESTED,
    role: 'risk',
  };
}

export const APPROVE_WORKSPACE_COMPLETED = 'APPOVE_WORKSPACE_COMPLETED';

export function approveWorkspaceCompleted(error) {
  return {
    type: APPROVE_WORKSPACE_COMPLETED,
    error,
  };
}

export const CHANGE_DB = 'CHANGE_DB';

export function changeDB(name) {
  return {
    type: CHANGE_DB,
    name,
  };
}

export const SET_MEMBERS = 'SET_MEMBERS';

export function setMembers(members) {
  return {
    type: SET_MEMBERS,
    members,
  };
}

export const ADD_MEMBER = 'ADD_MEMBER';

export function addMember() {
  return {
    type: ADD_MEMBER,
  };
}


export const NEW_MEMBER_FORM_CHANGED = 'NEW_MEMBER_FORM_CHANGED';

export function newMemberFormChanged(field) {
  return {
    type: NEW_MEMBER_FORM_CHANGED,
    field,
  };
}

export const REMOVE_MEMBER = 'REMOVE_MEMBER';

export function removeMember(username, role) {
  return {
    type: REMOVE_MEMBER,
    username,
    role
  };
}

export const FILTER_CHANGED = 'FILTER_CHANGED';

export function filterChanged(field) {
  return {
    type: FILTER_CHANGED,
    filter: field.filter.value
  };
}

export const SET_FILTERED_LIST = 'SET_FILTERED_LIST';

export function setFilteredList(workspaces) {
  return {
    type: SET_FILTERED_LIST,
    filteredList: workspaces,
  }
}

export const NEW_MEMBER_SELECTED = 'NEW_MEMBER_SELECTED';

export function newMemberSelected(username) {
  return {
    type: NEW_MEMBER_SELECTED,
    username,
  }
}

export const EXISTING_MEMBER_SELECTED = 'EXISTING_MEMBER_SELECTED';

export function existingMemberSelected(username) {
  return {
    type: EXISTING_MEMBER_SELECTED,
    username,
  }
}