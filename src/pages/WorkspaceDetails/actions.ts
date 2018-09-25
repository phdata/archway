import { Workspace, Member, YarnApplication, HiveTable } from '../../types/Workspace';

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

export const GET_APPLICATIONS = 'GET_APPLICATIONS';
export const getApplications = (id: number) => ({
  type: GET_APPLICATIONS,
});

export const SET_APPLICATIONS = 'SET_APPLICATIONS';
export const setApplications = (applications: YarnApplication[]) => ({
  type: SET_APPLICATIONS,
  applications,
});

export const GET_TABLES = 'GET_TABLES';
export const getTables = (id: number) => ({
  type: GET_TABLES,
});

export const SET_TABLES = 'SET_TABLES';
export const setTables = (tables: HiveTable[]) => ({
  type: SET_TABLES,
  tables,
});
