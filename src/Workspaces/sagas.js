import { call, select, all, fork, takeLatest, put } from 'redux-saga/effects';
import { delay } from 'redux-saga';
import { push } from 'react-router-redux';
import Fuse from 'fuse.js';
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
  ADD_MEMBER,
  REMOVE_MEMBER,
  FILTER_CHANGED,

  workspaceGenerated,
  setRequest,
  setGenerating,
  setWorkspace,
  setWorkspaceList,
  getWorkspace,
  approveWorkspaceCompleted,
  setMembers,
  changeDB,
  setFilteredList,
  filterChanged,
} from './actions';

const fuseOptions = {
  keys: [
    'name',
  ]
}

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
  yield put(setWorkspaceList(new Fuse(workspaces, fuseOptions)));
  yield put(filterChanged({ filter: { value: '' } }));
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

function* getMembers() {
  const token = yield select(s => s.auth.token);
  const workspace = yield select(s => s.workspaces.activeWorkspace);
  const id = workspace.id;
  const members = yield call(Api.getMembers, token, id);
  yield put(setMembers(members));
}

function* dbChanging() {
  yield takeLatest(CHANGE_DB, getMembers);
}

function* addMember() {
  const token = yield select(s => s.auth.token);
  const { activeWorkspace: { id }, newMemberForm: { username, role }, activeDatabase } = yield select(s => s.workspaces);
  yield call(Api.workspaceNewMember, token, id, activeDatabase, role, username);
  yield put(changeDB(activeDatabase));
}

function* memberRequested() {
  yield takeLatest(ADD_MEMBER, addMember);
}

function* removeMember({ username, role }) {
  const token = yield select(s => s.auth.token);
  const { activeWorkspace: { id }, activeDatabase } = yield select(s => s.workspaces);
  yield call(Api.removeWorkspaceMember, token, id, activeDatabase, role, username);
  yield put(changeDB(activeDatabase));
}

function* removeMemberRequested() {
  yield takeLatest(REMOVE_MEMBER, removeMember);
}

function* updateFilter({ filter }) {
  const workspaceList = yield select(s => s.workspaces.workspaceList);
  let filtered = workspaceList.list;
  if (filter && filter !== '')
    filtered = workspaceList.search(filter);
  yield put(setFilteredList(filtered));
}

function* filterChange() {
  yield takeLatest(FILTER_CHANGED, updateFilter);
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
    fork(memberRequested),
    fork(removeMemberRequested),
    fork(filterChange),
  ]);
}