import { all, call, fork, put, select, takeLatest, takeEvery } from 'redux-saga/effects';
import * as Api from '../../service/api';
import {
  GET_WORKSPACE,
  GET_USER_SUGGESTIONS,
  CHANGE_MEMBER_ROLE_REQUESTED,
  SIMPLE_MEMBER_REQUEST,
  REQUEST_APPROVAL,
  REQUEST_REMOVE_MEMBER,
  REQUEST_TOPIC,
  REQUEST_APPLICATION,
  REQUEST_REFRESH_YARN_APPS,
  REQUEST_REFRESH_HIVE_TABLES,
  REQUEST_DELETE_WORKSPACE,
  REQUEST_DEPROVISION_WORKSPACE,
  REQUEST_PROVISION_WORKSPACE,
  setWorkspace,
  setUserSuggestions,
  setMembers,
  setNamespaceInfo,
  setResourcePools,
  setActiveTopic,
  setActiveApplication,
  ApprovalRequestAction,
  approvalSuccess,
  topicRequestSuccess,
  SimpleMemberRequestAction,
  simpleMemberRequestComplete,
  ChangeMemberRoleRequestAction,
  changeMemberRoleRequestComplete,
  applicationRequestSuccess,
  getWorkspace,
  approvalFailure,
  RemoveMemberRequestAction,
  removeMemberSuccess,
  removeMemberFailure,
  refreshYarnAppsSuccess,
  refreshYarnAppsFailure,
  refreshHiveTablesSuccess,
  refreshHiveTablesFailure,
  setActiveModal,
  setMemberLoading,
  setProvisioning,
  setManageLoading,
  setNotificationStatus,
  clearNotificationStatus,
  setWorkspaceFetching,
  setUserSuggestionsLoading,
  REQUEST_CHANGE_WORKSPACE_OWNER,
  setOwnerLoading,
  REQUEST_MODIFY_DISK_QUOTA,
  setQuotaLoading,
} from './actions';
import { ErrorMessages } from './constants';
import { RECENT_WORKSPACES_KEY, TOKEN_EXTRACTOR, NotificationType } from '../../constants';

import { Workspace } from '../../models/Workspace';

/* tslint:disable:no-var-requires */
const router = require('connected-react-router/immutable');

export const detailExtractor = (s: any) => s.getIn(['details', 'details']);
export const memberRequestFormExtractor = (s: any) => s.getIn(['form', 'simpleMemberRequest', 'values']);
export const ownerDnExtractor = (s: any) => s.getIn(['form', 'changeOwnerRequest', 'values']);
export const quotaExtractor = (s: any) => s.getIn(['form', 'modifyDiskQuotaRequest', 'values']);
export const topicMemberRequestFormExtractor = (s: any) => s.getIn(['form', 'simpleTopicMemberRequest', 'values']);
export const activeTopicExtractor = (s: any) => s.getIn(['details', 'activeTopic']);

function* fetchUserSuggestions({ filter }: { type: string; filter: string }) {
  const token = yield select(TOKEN_EXTRACTOR);
  try {
    yield put(setUserSuggestionsLoading(true));
    const suggestions = yield call(Api.getUserSuggestions, token, filter);
    yield put(setUserSuggestions(filter, suggestions));
  } catch (e) {
    //
  } finally {
    yield put(setUserSuggestionsLoading(false));
  }
}

function* userSuggestionsRequest() {
  yield takeLatest(GET_USER_SUGGESTIONS, fetchUserSuggestions);
}

function* fetchWorkspace({ id }: { type: string; id: number }) {
  const token = yield select(TOKEN_EXTRACTOR);
  const recent = (yield select((s: any) => s.getIn(['home', 'recent']))).toJS();
  try {
    const workspace = yield call(Api.getWorkspace, token, id);
    yield put(setWorkspace(workspace));

    const { provisioning } = yield call(Api.getProvisioning, token, id);
    yield put(setProvisioning(provisioning));

    const recentWorkspaces = [workspace, ...recent.filter((w: Workspace) => w.id !== workspace.id)];
    localStorage.setItem(RECENT_WORKSPACES_KEY, JSON.stringify(recentWorkspaces.slice(0, 2)));

    const { members, resourcePools, infos } = yield all({
      members: call(Api.getMembers, token, id),
      resourcePools: call(Api.getYarnApplications, token, id),
      infos: call(Api.getHiveTables, token, id),
    });

    yield all([put(setMembers(members)), put(setNamespaceInfo(infos)), put(setResourcePools(resourcePools))]);

    if (workspace.topics.length > 0) {
      yield put(setActiveTopic(workspace.topics[0]));
    }
    if (workspace.applications.length > 0) {
      yield put(setActiveApplication(workspace.applications[0]));
    }
  } catch (err) {
    if (err === 403) {
      yield put(setNotificationStatus(NotificationType.Error, ErrorMessages.WorkspaceFetching));
      yield put(clearNotificationStatus());
      yield put(setWorkspaceFetching(false));
    }
  }
}

function* workspaceRequest() {
  yield takeLatest(GET_WORKSPACE, fetchWorkspace);
}

function* approvalRequested({ approvalType }: ApprovalRequestAction) {
  const token = yield select(TOKEN_EXTRACTOR);
  const { id } = yield select((s: any) =>
    s
      .get('details')
      .get('details')
      .toJS()
  );
  try {
    const approval = yield call(Api.approveWorkspace, token, id, approvalType);
    yield put(approvalSuccess(approvalType, approval));
  } catch (e) {
    yield put(approvalFailure(approvalType, e.toString()));
  }
}

function* approvalRequestedListener() {
  yield takeLatest(REQUEST_APPROVAL, approvalRequested);
}

function* topicRequested() {
  const token = yield select(TOKEN_EXTRACTOR);
  const { id } = yield select((s: any) =>
    s
      .get('details')
      .get('details')
      .toJS()
  );
  const { name } = yield select((s: any) => s.getIn(['form', 'topicRequest', 'values']).toJS());
  yield call(Api.requestTopic, token, id, name, 1, 1);
  yield put(topicRequestSuccess());
  yield put(getWorkspace(id));
}

function* topicRequestedListener() {
  yield takeLatest(REQUEST_TOPIC, topicRequested);
}

function* applicationRequested() {
  const token = yield select(TOKEN_EXTRACTOR);
  const { id } = yield select((s: any) =>
    s
      .get('details')
      .get('details')
      .toJS()
  );
  const { name, application_type, logo, language, repository } = yield select((s: any) =>
    s.getIn(['form', 'applicationRequest', 'values']).toJS()
  );
  try {
    yield call(Api.requestApplication, token, id, name, application_type, logo, language, repository);
    yield put(applicationRequestSuccess());
    yield put(getWorkspace(id));
    yield put(setActiveModal(false));
  } catch (e) {
    //
  }
}

function* applicationRequestedListener() {
  yield takeLatest(REQUEST_APPLICATION, applicationRequested);
}

export function* simpleMemberRequested({ resource }: SimpleMemberRequestAction) {
  const token = yield select(TOKEN_EXTRACTOR);
  const workspace = (yield select(detailExtractor)).toJS();
  try {
    yield put(setMemberLoading(true));
    if (resource === 'data') {
      const { username, roles } = (yield select(memberRequestFormExtractor)).toJS();
      yield all(
        workspace.data.map(({ id, name }: { id: number; name: string }) => {
          const role = roles[name] || 'readonly';
          return role !== 'none' && call(Api.newWorkspaceMember, token, workspace.id, resource, id, role, username);
        })
      );
      yield put(simpleMemberRequestComplete());
    } else if (resource === 'topics') {
      const { id } = yield select(activeTopicExtractor);
      const { username, role } = (yield select(topicMemberRequestFormExtractor)).toJS();
      yield call(Api.newWorkspaceMember, token, workspace.id, resource, id, role, username);
      yield put(simpleMemberRequestComplete());
    }
    const members = yield call(Api.getMembers, token, workspace.id);
    yield put(setMembers(members));
  } catch (e) {
    yield put(setNotificationStatus(NotificationType.Error, ErrorMessages.AddingMember));
    yield put(simpleMemberRequestComplete());
  } finally {
    yield put(setMemberLoading(false));
  }
}

function* simpleMemberRequestedListener() {
  yield takeLatest(SIMPLE_MEMBER_REQUEST, simpleMemberRequested);
}

export function* changeMemberRoleRequested({
  distinguished_name,
  roleId,
  role,
  resource,
}: ChangeMemberRoleRequestAction) {
  const token = yield select(TOKEN_EXTRACTOR);
  const workspace = (yield select(detailExtractor)).toJS();
  yield call(Api.removeWorkspaceMember, token, workspace.id, resource, roleId, 'none', distinguished_name);
  yield call(Api.newWorkspaceMember, token, workspace.id, resource, roleId, role, distinguished_name);

  yield put(changeMemberRoleRequestComplete());
  const members = yield call(Api.getMembers, token, workspace.id);
  yield put(setMembers(members));
}

function* changeMemberRoleRequestedListener() {
  yield takeLatest(CHANGE_MEMBER_ROLE_REQUESTED, changeMemberRoleRequested);
}

export function* removeMemberRequested({ distinguished_name, roleId, resource }: RemoveMemberRequestAction) {
  const token = yield select(TOKEN_EXTRACTOR);
  const workspace = (yield select(detailExtractor)).toJS();
  try {
    yield call(Api.removeWorkspaceMember, token, workspace.id, resource, roleId, 'none', distinguished_name);
    yield put(removeMemberSuccess(distinguished_name));
    const members = yield call(Api.getMembers, token, workspace.id);
    yield put(setMembers(members));
  } catch (e) {
    yield put(removeMemberFailure(distinguished_name, e.toString()));
  }
}

function* removeMemberRequestedListener() {
  yield takeEvery(REQUEST_REMOVE_MEMBER, removeMemberRequested);
}

function* refreshYarnAppsRequested() {
  const token = yield select(TOKEN_EXTRACTOR);
  const { id } = yield select((s: any) =>
    s
      .get('details')
      .get('details')
      .toJS()
  );
  try {
    const apps = yield call(Api.getYarnApplications, token, id);
    yield put(refreshYarnAppsSuccess(apps));
  } catch (e) {
    yield put(refreshYarnAppsFailure(e.toString()));
  }
}

function* refreshYarnAppsRequestedListener() {
  yield takeLatest(REQUEST_REFRESH_YARN_APPS, refreshYarnAppsRequested);
}

function* refreshHiveTablesRequested() {
  const token = yield select(TOKEN_EXTRACTOR);
  const { id } = yield select((s: any) =>
    s
      .get('details')
      .get('details')
      .toJS()
  );
  try {
    const apps = yield call(Api.getHiveTables, token, id);
    yield put(refreshHiveTablesSuccess(apps));
  } catch (e) {
    yield put(refreshHiveTablesFailure(e.toString()));
  }
}

function* refreshHiveTablesRequestedListener() {
  yield takeLatest(REQUEST_REFRESH_HIVE_TABLES, refreshHiveTablesRequested);
}

function* deleteWorkspaceRequested() {
  const token = yield select(TOKEN_EXTRACTOR);
  const id = (yield select(detailExtractor)).toJS().id;
  try {
    yield put(setManageLoading('delete', true));
    yield call(Api.deleteWorkspace, token, id);
    yield put(setNotificationStatus(NotificationType.Success, `Success: Delete workspace ${id}`));
    yield put(router.push({ pathname: '/operations' }));
  } catch {
    yield put(setNotificationStatus(NotificationType.Error, `Failed to delete workspace ${id}`));
  } finally {
    yield put(setManageLoading('delete', false));
    yield put(setActiveModal(false));
    yield put(clearNotificationStatus());
  }
}

function* deleteWorkspaceRequestedListener() {
  yield takeLatest(REQUEST_DELETE_WORKSPACE, deleteWorkspaceRequested);
}

function* deprovisionWorkspaceRequested() {
  const token = yield select(TOKEN_EXTRACTOR);
  const id = (yield select(detailExtractor)).toJS().id;
  try {
    yield put(setManageLoading('deprovision', true));
    yield call(Api.deprovisionWorkspace, token, id);
    yield put(setNotificationStatus(NotificationType.Success, `Success: Deprovision workspace ${id}`));
  } catch {
    yield put(setNotificationStatus(NotificationType.Error, `Failed to deprovision workspace ${id}`));
  } finally {
    yield put(setManageLoading('deprovision', false));
    yield put(setActiveModal(false));
    yield put(clearNotificationStatus());
  }
}

function* deprovisionWorkspaceRequestedListener() {
  yield takeLatest(REQUEST_DEPROVISION_WORKSPACE, deprovisionWorkspaceRequested);
}

function* provisionWorkspaceRequested() {
  const token = yield select(TOKEN_EXTRACTOR);
  const id = (yield select(detailExtractor)).toJS().id;
  try {
    yield put(setManageLoading('provision', true));
    yield call(Api.provisionWorkspace, token, id);
    yield put(setNotificationStatus(NotificationType.Success, `Success: Provision workspace ${id}`));
  } catch {
    yield put(setNotificationStatus(NotificationType.Error, `Failed to provision workspace ${id}`));
  } finally {
    yield put(setManageLoading('provision', false));
    yield put(setActiveModal(false));
    yield put(clearNotificationStatus());
  }
}

function* provisionWorkspaceRequestedListener() {
  yield takeLatest(REQUEST_PROVISION_WORKSPACE, provisionWorkspaceRequested);
}

function* changeWorkspaceOwnerRequested() {
  const token = yield select(TOKEN_EXTRACTOR);
  const { id, name } = (yield select(detailExtractor)).toJS();
  const ownerDn = (yield select(ownerDnExtractor)).toJS().username;
  try {
    yield put(setOwnerLoading(true));
    yield call(Api.changeWorkspaceOwner, token, id, ownerDn);
  } catch {
    yield put(setNotificationStatus(NotificationType.Error, `Failed to change the owner of ${name}`));
  } finally {
    yield put(clearNotificationStatus());
    yield put(setOwnerLoading(false));
    yield put(setActiveModal(false));
  }
}

function* changeWorkspaceOwnerRequestedListener() {
  yield takeLatest(REQUEST_CHANGE_WORKSPACE_OWNER, changeWorkspaceOwnerRequested);
}

function* modifyDiskQuotaRequested() {
  const token = yield select(TOKEN_EXTRACTOR);
  const { id, name } = (yield select(detailExtractor)).toJS();
  const quota = (yield select(quotaExtractor)).toJS().quota;
  try {
    yield put(setQuotaLoading(true));
    yield call(Api.modifyDiskQuota, token, id, quota);
  } catch {
    yield put(setNotificationStatus(NotificationType.Error, `Failed to modify disk quota of ${name}`));
  } finally {
    yield put(clearNotificationStatus());
    yield put(setQuotaLoading(false));
    yield put(setActiveModal(false));
  }
}

function* modifyDiskQuotaRequestedListener() {
  yield takeLatest(REQUEST_MODIFY_DISK_QUOTA, modifyDiskQuotaRequested);
}

export default function* root() {
  yield all([
    fork(workspaceRequest),
    fork(userSuggestionsRequest),
    fork(approvalRequestedListener),
    fork(topicRequestedListener),
    fork(simpleMemberRequestedListener),
    fork(changeMemberRoleRequestedListener),
    fork(removeMemberRequestedListener),
    fork(refreshYarnAppsRequestedListener),
    fork(refreshHiveTablesRequestedListener),
    fork(applicationRequestedListener),
    fork(deleteWorkspaceRequestedListener),
    fork(deprovisionWorkspaceRequestedListener),
    fork(provisionWorkspaceRequestedListener),
    fork(changeWorkspaceOwnerRequestedListener),
    fork(modifyDiskQuotaRequestedListener),
  ]);
}
