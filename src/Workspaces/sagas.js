import { call, select, all, fork, takeLatest, put } from 'redux-saga/effects';
import { delay } from 'redux-saga';
import { push } from 'react-router-redux';
import * as Api from '../API';
import {
  SET_REQUEST_TYPE,
  SET_REQUEST,
  REQUEST_CHANGED,
  WORKSPACE_REQUESTED,
  LIST_WORKSPACES,
  GET_WORKSPACE,
  APPROVE_WORKSPACE_REQUESTED,
  CHANGE_DB,

  workspaceGenerated,
  setRequest,
  setGenerating,
  setWorkspace,
  setWorkspaceList,
  getWorkspace,
  approveWorkspaceCompleted,
  setMembers,
  changeDB,
} from './actions';

function* handleTypeChanged({ requestType }) {
  yield put(setGenerating(true));
  const token = yield select(s => s.auth.token);
  const templateDefaults = yield call(Api.getTemplate, token, requestType);
  yield put(setRequest(templateDefaults));
}

function* typeChanged() {
  yield takeLatest(SET_REQUEST_TYPE, handleTypeChanged);
}

function* updateWorkspace({ request }) {
  yield delay(500);
  const token = yield select(s => s.auth.token);
  const requestType = yield select(s => s.workspaces.pendingRequestType);
  const workspace = yield call(Api.processTemplate, token, requestType, request);
  yield put(workspaceGenerated(workspace));
  yield put(setGenerating(false));
}

function* requestChanged() {
  yield takeLatest(SET_REQUEST, updateWorkspace);
}

function* updateRequest() {
  yield put(setGenerating(true));
  const request = yield select(s => s.workspaces.pendingRequest);
  yield put(setRequest(request));
}

function* inputChanged() {
  yield takeLatest(REQUEST_CHANGED, updateRequest);
}

function* requestWorkspace() {
  const token = yield select(s => s.auth.token);
  const workspace = yield select(s => s.workspaces.pendingWorkspace);
  const newWorkspace = yield call(Api.requestWorkspace, token, workspace);
  yield put(push({ pathname: `/workspaces/${newWorkspace.id}` }));
}

function* workspaceRequested() {
  yield takeLatest(WORKSPACE_REQUESTED, requestWorkspace);
}

function* getAllWorkspaces() {
  const token = yield select(s => s.auth.token);
  const workspaces = yield call(Api.listWorkspaces, token);
  yield put(setWorkspaceList(workspaces));
}

function* listWorkspaces() {
  yield takeLatest(LIST_WORKSPACES, getAllWorkspaces);
}

function* fetchWorkspace({ id }) {
  const token = yield select(s => s.auth.token);
  const workspace = yield call(Api.getWorkspace, token, id);
  yield put(setWorkspace(workspace));
  if (workspace.data.length > 0) { yield put(changeDB(workspace.data[0].name)); }
}

function* workspaceRequest() {
  yield takeLatest(GET_WORKSPACE, fetchWorkspace);
}

function* requestApproval({ role }) {
  const token = yield select(s => s.auth.token);
  const id = yield select(s => s.workspaces.activeWorkspace.id);
  let error;
  try {
    yield call(Api.approveWorkspace, token, id, role);
  } catch (e) {
    error = e;
  }
  yield put(approveWorkspaceCompleted(error));
  yield put(getWorkspace(id));
}

function* approveWorkspace() {
  yield takeLatest(APPROVE_WORKSPACE_REQUESTED, requestApproval);
}

function* getMembers({ name }) {
  const token = yield select(s => s.auth.token);
  const id = yield select(s => s.workspaces.activeWorkspace.id);
  const { managers, readonly } = yield all({
    managers: call(Api.getMembers, token, id, name, 'managers'),
    readonly: call(Api.getMembers, token, id, name, 'readonly'),
  });
  yield all([put(setMembers('managers', managers)), put(setMembers('readonly', readonly))]);
}

function* dbChanging() {
  yield takeLatest(CHANGE_DB, getMembers);
}

export default function* root() {
  yield all([
    fork(typeChanged),
    fork(requestChanged),
    fork(inputChanged),
    fork(workspaceRequested),
    fork(listWorkspaces),
    fork(workspaceRequest),
    fork(approveWorkspace),
    fork(dbChanging),
  ]);
}
