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
