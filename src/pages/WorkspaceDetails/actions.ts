import { Workspace, Member } from '../../types/Workspace';

export const GET_WORKSPACE = 'GET_WORKSPACE';
export const getWorkspace = (id: number) => ({
  type: GET_WORKSPACE,
  id,
});

export const SET_WORKSPACE = 'SET_WORKSPACE';
export const setWorkspace = (workspace: Workspace) => ({
  type: SET_WORKSPACE,
  workspace,
});

export const GET_MEMBERS = 'GET_MEMBERS';
export const getMembers = (id: number) => ({
  type: GET_MEMBERS,
  id,
});

export const SET_MEMBERS = 'SET_MEMBERS';
export const setMembers = (members: Member[]) => ({
  type: SET_MEMBERS,
  members,
});
