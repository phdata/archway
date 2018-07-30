export const GET_WORKSPACE = 'GET_WORKSPACE';

export function getWorkspace(id) {
  return {
    type: GET_WORKSPACE,
    id,
  };
}

export const SET_WORKSPACE = 'SET_WORKSPACE';

export function setWorkspace(workspace) {
  return {
    type: SET_WORKSPACE,
    workspace
  };
}
