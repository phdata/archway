import { delay } from 'redux-saga';
import { all, call, fork, put, select, takeLatest } from 'redux-saga/effects';
import * as Api from '../../api';
import { RequestInput } from '../../types/RequestInput';
import {
  setGenerating,
  setTemplate,
  workspaceGenerated,
  SET_BEHAVIOR,
  SET_REQUEST,
  WORKSPACE_REQUESTED,
} from './actions';

/* tslint:disable:no-var-requires */
const router = require('connected-react-router/immutable');

function* behaviorChangedHandler({ behavior }: any) {
  yield put(setGenerating(true));
  const token = yield select((s: any) => s.get('login').get('token'));
  const templateDefaults = yield call(Api.getTemplate, token, behavior);
  yield put(setTemplate(templateDefaults));
}

function* behaviorChanged() {
  yield takeLatest(SET_BEHAVIOR, behaviorChangedHandler);
}

function* requestChangedHandler({ request }: { type: string, request: RequestInput }) {
  yield put(setGenerating(true));
  yield delay(500);
  const token = yield select((s: any) => s.get('login').get('token'));
  const requestType = yield select((s: any) => s.get('request').get('behavior'));
  const template = yield select((s: any) => s.get('request').get('template'));
  const workspaceRequest = Object.assign(template, request);
  const workspace = yield call(Api.processTemplate, token, requestType, workspaceRequest);
  yield put(workspaceGenerated(workspace));
  yield put(setGenerating(false));
}

function* requestChanged() {
  yield takeLatest(SET_REQUEST, requestChangedHandler);
}

function* workspaceRequestedHandler() {
  const token = yield select((s: any) => s.get('login').get('token'));
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
    fork(workspaceRequested),
  ]);
}
