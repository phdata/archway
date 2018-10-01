import {
  Workspace,
  Member,
  NamespaceInfo,
  PoolInfo,
} from '../../types/Workspace';

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

export const SET_RESOURCE_POOLS = 'SET_RESOURCE_POOLS';
export const setResourcePools = (resourcePools: PoolInfo[]) => ({
  type: SET_RESOURCE_POOLS,
  resourcePools,
});

export const GET_TABLES = 'GET_TABLES';
export const getTables = (id: number) => ({
  type: GET_TABLES,
});

export const SET_NAMESPACE_INFO = 'SET_NAMESPACE_INFO';
export const setNamespaceInfo = (infos: NamespaceInfo[]) => ({
  type: SET_NAMESPACE_INFO,
  infos,
});

export const SET_ACTIVE_MODAL = 'SET_ACTIVE_MODAL';
export const setActiveModal = (activeModal: string | boolean) => ({
  type: SET_ACTIVE_MODAL,
  activeModal,
});
