import { Workspace } from '../../types/Workspace';

export const GET_WORKSPACE = 'GET_WORKSPACE';
export function getWorkspace(id: number) {
  return {
    type: GET_WORKSPACE,
    id,
  };
}

export const SET_WORKSPACE = 'SET_WORKSPACE';
export function setWorkspace(workspace: Workspace) {
  return {
    type: SET_WORKSPACE,
    workspace,
  };
}
