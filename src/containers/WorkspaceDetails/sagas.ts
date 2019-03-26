import { all, call, fork, put, select, takeLatest, takeEvery } from 'redux-saga/effects';
import * as Api from '../../service/api';
import {
  REQUEST_APPROVAL,
  REQUEST_REMOVE_MEMBER,
  GET_WORKSPACE,
  setWorkspace,
  GET_USER_SUGGESTIONS,
  setUserSuggestions,
  setMembers,
  setNamespaceInfo,
  setResourcePools,
  setActiveTopic,
  ApprovalRequestAction,
  approvalSuccess,
  REQUEST_TOPIC,
  topicRequestSuccess,
  SIMPLE_MEMBER_REQUEST,
  SimpleMemberRequestAction,
  simpleMemberRequestComplete,
  CHANGE_MEMBER_ROLE_REQUESTED,
  ChangeMemberRoleRequestAction,
  changeMemberRoleRequestComplete,
  getWorkspace,
  approvalFailure,
  RemoveMemberRequestAction,
  removeMemberSuccess,
  removeMemberFailure,
  REQUEST_REFRESH_YARN_APPS,
  refreshYarnAppsSuccess,
  refreshYarnAppsFailure,
  REQUEST_REFRESH_HIVE_TABLES,
  refreshHiveTablesSuccess,
  refreshHiveTablesFailure,
} from './actions';

export const tokenExtractor = (s: any) => s.getIn(['login', 'token']);
export const detailExtractor = (s: any) => s.getIn(['details', 'details']);
export const memberRequestFormExtractor = (s: any) => s.getIn(['form', 'simpleMemberRequest', 'values']);
export const topicMemberRequestFormExtractor = (s: any) => s.getIn(['form', 'simpleTopicMemberRequest', 'values']);
export const activeTopicExtractor = (s: any) => s.getIn(['details', 'activeTopic']);

function* fetchUserSuggestions({ filter }: { type: string, filter: string }) {
  const token = yield select((s: any) => s.get('login').get('token'));
  try {
    const suggestions = yield call(Api.getUserSuggestions, token, filter);
    yield put(setUserSuggestions(filter, suggestions));
  } catch (e) {
    //
  }
}

function* userSuggestionsRequest() {
  yield takeLatest(GET_USER_SUGGESTIONS, fetchUserSuggestions);
}

function* fetchWorkspace({ id }: { type: string, id: number }) {
  const token = yield select((s: any) => s.get('login').get('token'));
  const workspace = yield call(Api.getWorkspace, token, id);
  yield put(setWorkspace(workspace));

  const { members, resourcePools, infos } = yield all({
    members: call(Api.getMembers, token, id),
    resourcePools: call(Api.getYarnApplications, token, id),
    infos: call(Api.getHiveTables, token, id),
  });

  yield all([
    put(setMembers(members)),
    put(setNamespaceInfo(infos)),
    put(setResourcePools(resourcePools)),
  ]);

  if (workspace.topics.length > 0) {
    yield put(setActiveTopic(workspace.topics[0]));
  }
}

function* workspaceRequest() {
  yield takeLatest(GET_WORKSPACE, fetchWorkspace);
}

function* approvalRequested({ approvalType }: ApprovalRequestAction) {
  const token = yield select((s: any) => s.get('login').get('token'));
  const { id } = yield select((s: any) => s.get('details').get('details').toJS());
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
  const token = yield select((s: any) => s.get('login').get('token'));
  const { id } = yield select((s: any) => s.get('details').get('details').toJS());
  const { name } = yield select((s: any) => s.getIn(['form', 'topicRequest', 'values']).toJS());
  yield call(Api.requestTopic, token, id, name, 1, 1);
  yield put(topicRequestSuccess());
  yield put(getWorkspace(id));
}

function* topicRequestedListener() {
  yield takeLatest(REQUEST_TOPIC, topicRequested);
}

export function* simpleMemberRequested({ resource }: SimpleMemberRequestAction) {
  const token = yield select(tokenExtractor);
  const workspace = (yield select(detailExtractor)).toJS();
  try {
    if (resource === 'data') {
      const { username, roles } = (yield select(memberRequestFormExtractor)).toJS();
      yield all(
        workspace.data.map(({ id, name }: { id: number, name: string }) => {
          const role = roles[name] || 'readonly';
          return (role !== 'none') && call(Api.newWorkspaceMember, token, workspace.id, resource, id, role, username);
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
    //
  }
}

function* simpleMemberRequestedListener() {
  yield takeLatest(SIMPLE_MEMBER_REQUEST, simpleMemberRequested);
}

export function* changeMemberRoleRequested({ distinguished_name, roleId, role, resource }: ChangeMemberRoleRequestAction) {
  const token = yield select(tokenExtractor);
  const workspace = (yield select(detailExtractor)).toJS();
  try {
    yield call(Api.removeWorkspaceMember, token, workspace.id, resource, roleId, role, distinguished_name)
    if (role !== 'none') {
      yield call(Api.newWorkspaceMember, token, workspace.id, resource, roleId, role, distinguished_name);
    }
    yield put(changeMemberRoleRequestComplete());
    const members = yield call(Api.getMembers, token, workspace.id);
    yield put(setMembers(members));
  } catch (e) {
    //
  }
}

function* changeMemberRoleRequestedListener() {
  yield takeLatest(CHANGE_MEMBER_ROLE_REQUESTED, changeMemberRoleRequested);
}

export function* removeMemberRequested({ distinguished_name, role }: RemoveMemberRequestAction) {
  const token = yield select(tokenExtractor);
  const workspace = (yield select(detailExtractor)).toJS();
  try {
    yield all(
      workspace.data.map(({ id }: { id: number }) => (
        call(Api.removeWorkspaceMember, token, workspace.id, 'data', id, role, distinguished_name)
      )),
    );
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
  const token = yield select((s: any) => s.get('login').get('token'));
  const { id } = yield select((s: any) => s.get('details').get('details').toJS());
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
  const token = yield select((s: any) => s.get('login').get('token'));
  const { id } = yield select((s: any) => s.get('details').get('details').toJS());
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
  ]);
}
