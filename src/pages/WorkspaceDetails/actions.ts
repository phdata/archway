import {
  Workspace,
  Member,
  NamespaceInfo,
  PoolInfo,
  ApprovalItem,
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

export const REQUEST_APPROVAL = 'REQUEST_APPROVAL';
type ApprovalType = 'infra' | 'risk';
export interface ApprovalRequestAction {
  type: typeof REQUEST_APPROVAL;
  approvalType: ApprovalType;
}
export const requestApproval = (approvalType: ApprovalType) => ({
  type: REQUEST_APPROVAL,
  approvalType,
});

export const APPROVAL_SUCCESS = 'APPROVAL_SUCCESS';
export interface ApprovalSuccessAction {
  type: typeof APPROVAL_SUCCESS;
  approvalType: ApprovalType;
  approval: ApprovalItem;
}
export const approvalSuccess = (approvalType: ApprovalType, approval: ApprovalItem) => ({
  type: APPROVAL_SUCCESS,
  approvalType,
  approval,
});
