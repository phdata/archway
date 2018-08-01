import { call, select, all, fork, takeLatest, put } from 'redux-saga/effects';
import { push } from 'react-router-redux';
import { delay } from 'redux-saga';

import * as Api from '../../API';
import {
  SET_REQUEST_TYPE,
  SET_REQUEST,
  REQUEST_CHANGED,
  WORKSPACE_REQUESTED,

  setGenerating,
  setRequest,
  workspaceGenerated,
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
  const requestType = yield select(s => s.workspaces.request.pendingRequestType);
  const workspace = yield call(Api.processTemplate, token, requestType, request);
  yield put(workspaceGenerated(workspace));
  yield put(setGenerating(false));
}

function* requestChanged() {
  yield takeLatest(SET_REQUEST, updateWorkspace);
}

function* updateRequest() {
  yield put(setGenerating(true));
  const request = yield select(s => s.workspaces.request.pendingRequest);
  yield put(setRequest(request));
}

function* inputChanged() {
  yield takeLatest(REQUEST_CHANGED, updateRequest);
}

function* requestWorkspace() {
  const token = yield select(s => s.auth.token);
  const workspace = yield select(s => s.workspaces.request.pendingWorkspace);
  const newWorkspace = yield call(Api.requestWorkspace, token, workspace);
  yield put(push({ pathname: `/workspaces/${newWorkspace.id}` }));
}

function* workspaceRequested() {
  yield takeLatest(WORKSPACE_REQUESTED, requestWorkspace);
}

export default function* root() {
  yield all([
    fork(typeChanged),
    fork(requestChanged),
    fork(inputChanged),
    fork(workspaceRequested)
  ])
}