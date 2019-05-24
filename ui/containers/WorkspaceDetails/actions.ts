import {
  Workspace,
  HiveAllocation,
  Member,
  NamespaceInfo,
  PoolInfo,
  ApprovalItem,
  KafkaTopic,
  Application,
} from '../../models/Workspace';

export const CLEAR_DETAILS = 'CLEAR_DETAILS';
export const clearDetails = () => ({
  type: CLEAR_DETAILS,
});

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

export const GET_USER_SUGGESTIONS = 'GET_USER_SUGGESTIONS';
export const getUserSuggestions = (filter: string) => ({
  type: GET_USER_SUGGESTIONS,
  filter,
});

export const SET_USER_SUGGESTIONS = 'SET_USER_SUGGESTIONS';
export const setUserSuggestions = (filter: string, suggestions: any) => ({
  type: SET_USER_SUGGESTIONS,
  filter,
  suggestions,
});

export const UPDATE_SELECTED_ALLOCATION = 'UPDATE_SELECTED_ALLOCATION';
export const updateSelectedAllocation = (allocation: HiveAllocation) => ({
  type: UPDATE_SELECTED_ALLOCATION,
  allocation,
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

export const SET_ACTIVE_TOPIC = 'SET_ACTIVE_TOPIC';
export const setActiveTopic = (activeTopic: KafkaTopic) => ({
  type: SET_ACTIVE_TOPIC,
  activeTopic,
});

export const SET_ACTIVE_APPLICATION = 'SET_ACTIVE_APPLICATION';
export const setActiveApplication = (activeApplication: Application) => ({
  type: SET_ACTIVE_APPLICATION,
  activeApplication,
});

export const REQUEST_APPROVAL = 'REQUEST_APPROVAL';
type ApprovalType = 'infra' | 'risk';
export interface ApprovalRequestAction {
  type: typeof REQUEST_APPROVAL;
  approvalType: ApprovalType;
}
export const requestApproval = (approvalType: ApprovalType): ApprovalRequestAction => ({
  type: REQUEST_APPROVAL,
  approvalType,
});

export const APPROVAL_SUCCESS = 'APPROVAL_SUCCESS';
export interface ApprovalSuccessAction {
  type: typeof APPROVAL_SUCCESS;
  approvalType: ApprovalType;
  approval: ApprovalItem;
}
export const approvalSuccess = (approvalType: ApprovalType, approval: ApprovalItem): ApprovalSuccessAction => ({
  type: APPROVAL_SUCCESS,
  approvalType,
  approval,
});

export const REQUEST_TOPIC = 'REQUEST_TOPIC';
export interface RequestTopicAction {
  type: typeof REQUEST_TOPIC;
}
export const requestTopic = (): RequestTopicAction => ({
  type: REQUEST_TOPIC,
});

export const TOPIC_REQUEST_SUCCESS = 'TOPIC_REQUEST_SUCCESS';
export interface TopicRequestSuccessAction {
  type: typeof TOPIC_REQUEST_SUCCESS;
}
export const topicRequestSuccess = () => ({
  type: TOPIC_REQUEST_SUCCESS,
});

export const SIMPLE_MEMBER_REQUEST = 'SIMPLE_MEMBER_REQUEST';
export interface SimpleMemberRequestAction {
  type: typeof SIMPLE_MEMBER_REQUEST;
  resource: string;
}
export const simpleMemberRequest = (resource: string) => ({
  type: SIMPLE_MEMBER_REQUEST,
  resource,
});

export const SIMPLE_MEMBER_REQUEST_COMPLETE = 'SIMPLE_MEMBER_REQUEST_COMPLETE';
export interface SimpleMemberRequestCompleteAction {
  type: typeof SIMPLE_MEMBER_REQUEST_COMPLETE;
}
export const simpleMemberRequestComplete = () => ({
  type: SIMPLE_MEMBER_REQUEST_COMPLETE,
});

export const CHANGE_MEMBER_ROLE_REQUESTED = 'CHANGE_MEMBER_ROLE_REQUESTED';
export interface ChangeMemberRoleRequestAction {
  type: typeof CHANGE_MEMBER_ROLE_REQUESTED;
  username: string;
  distinguished_name: string;
  roleId: number;
  role: string;
  resource: string;
}
export const changeMemberRoleRequest = (distinguished_name: string, roleId: number, role: string, resource: string) => ({
  type: CHANGE_MEMBER_ROLE_REQUESTED,
  distinguished_name,
  roleId,
  role,
  resource,
});

export const CHANGE_MEMBER_ROLE_REQUESTED_COMPLETE = 'CHANGE_MEMBER_ROLE_REQUESTED_COMPLETE';
export interface ChangeMemberRoleRequestCompleteAction {
  type: typeof CHANGE_MEMBER_ROLE_REQUESTED_COMPLETE;
}
export const changeMemberRoleRequestComplete = () => ({
  type: CHANGE_MEMBER_ROLE_REQUESTED_COMPLETE,
});

export const APPROVAL_FAILURE = 'APPROVAL_FAILURE';
export const approvalFailure = (approvalType: ApprovalType, error: string) => ({
  type: APPROVAL_FAILURE,
  approvalType,
});

export const REQUEST_REMOVE_MEMBER = 'REQUEST_REMOVE_MEMBER';
export interface RemoveMemberRequestAction {
  type: typeof REQUEST_REMOVE_MEMBER;
  distinguished_name: string;
  roleId: number;
  resource: string;
}
export const requestRemoveMember = (distinguished_name: string, roleId: number, resource: string) => ({
  type: REQUEST_REMOVE_MEMBER,
  distinguished_name,
  roleId,
  resource,
});

export const REMOVE_MEMBER_SUCCESS = 'REMOVE_MEMBER_SUCCESS';
export interface RemoveMemberSuccessAction {
  type: typeof REMOVE_MEMBER_SUCCESS;
  distinguished_name: string;
}
export const removeMemberSuccess = (distinguished_name: string) => ({
  type: REMOVE_MEMBER_SUCCESS,
  distinguished_name,
});

export const REMOVE_MEMBER_FAILURE = 'REMOVE_MEMBER_FAILURE';
export const removeMemberFailure = (distinguished_name: string, error: string) => ({
  type: REMOVE_MEMBER_FAILURE,
  distinguished_name,
  error,
});

export const REQUEST_REFRESH_YARN_APPS = 'REQUEST_REFRESH_YARN_APPS';
export const requestRefreshYarnApps = () => ({
  type: REQUEST_REFRESH_YARN_APPS,
});

export const REQUEST_APPLICATION = 'REQUEST_APPLICATION';
export interface RequestApplicationAction {
  type: typeof REQUEST_APPLICATION;
}
export const requestApplication = (): RequestApplicationAction => ({
  type: REQUEST_APPLICATION,
});

export const APPLICATION_REQUEST_SUCCESS = 'APPLICATION_REQUEST_SUCCESS';
export interface ApplicationRequestSuccessAction {
  type: typeof APPLICATION_REQUEST_SUCCESS;
}
export const applicationRequestSuccess = () => ({
  type: APPLICATION_REQUEST_SUCCESS,
});

export const REFRESH_YARN_APPS_SUCCESS = 'REFRESH_YARN_APPS_SUCCESS';
export const refreshYarnAppsSuccess = (apps: PoolInfo[]) => ({
  type: REFRESH_YARN_APPS_SUCCESS,
  apps,
});

export const REFRESH_YARN_APPS_FAILURE = 'REFRESH_YARN_APPS_FAILURE';
export const refreshYarnAppsFailure = (error: string) => ({
  type: REFRESH_YARN_APPS_FAILURE,
  error,
});

export const REQUEST_REFRESH_HIVE_TABLES = 'REQUEST_REFRESH_HIVE_TABLES';
export const requestRefreshHiveTables = () => ({
  type: REQUEST_REFRESH_HIVE_TABLES,
});

export const REFRESH_HIVE_TABLES_SUCCESS = 'REFRESH_HIVE_TABLES_SUCCESS';
export const refreshHiveTablesSuccess = (tables: NamespaceInfo[]) => ({
  type: REFRESH_HIVE_TABLES_SUCCESS,
  tables,
});

export const REFRESH_HIVE_TABLES_FAILURE = 'REFRESH_HIVE_TABLES_FAILURE';
export const refreshHiveTablesFailure = (error: string) => ({
  type: REFRESH_HIVE_TABLES_FAILURE,
  error,
});
