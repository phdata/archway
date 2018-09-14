import { call, select, all, fork, takeLatest, put } from 'redux-saga/effects';
const router = require('connected-react-router/immutable');
import { delay } from 'redux-saga';

import * as Api from '../API';
import {
  SET_BEHAVIOR,
  SET_REQUEST,
  WORKSPACE_REQUESTED,

  setGenerating,
  setRequest,
  workspaceGenerated,
} from './actions';

function* behaviorChangedHandler({ behavior }: any) {
  yield put(setGenerating(true));
  const token = yield select((s: any) => s.get('auth').get('token'));
  const templateDefaults = yield call(Api.getTemplate, token, behavior);
  yield put(setRequest(templateDefaults));
}

function* behaviorChanged() {
  yield takeLatest(SET_BEHAVIOR, behaviorChangedHandler);
}

function* requestChangedHandler({ request }: any) {
  yield put(setGenerating(true));
  yield delay(500);
  const token = yield select((s: any) => s.get('auth').get('token'));
  const requestType = yield select((s: any) => s.get('request').get('behavior'));
  const workspace = yield call(Api.processTemplate, token, requestType, request);
  yield put(workspaceGenerated(workspace));
  yield put(setGenerating(false));
}

function* requestChanged() {
  yield takeLatest(SET_REQUEST, requestChangedHandler);
}

function* workspaceRequestedHandler() {
  const token = yield select((s: any) => s.get('auth').get('token'));
  const workspace = yield select((s: any) => s.get('request').get('workspace'));
  const newWorkspace = yield call(Api.requestWorkspace, token, workspace);
  yield put(router.push({ pathname: `/workspaces/${newWorkspace.id}` }));
}

function* workspaceRequested() {
  yield takeLatest(WORKSPACE_REQUESTED, workspaceRequestedHandler);
}

export default function* root() {
  yield all([
    fork(behaviorChanged),
    fork(requestChanged),
    fork(workspaceRequested)
  ])
}