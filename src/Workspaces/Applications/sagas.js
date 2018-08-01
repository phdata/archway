import { call, select, all, fork, takeLatest, put } from 'redux-saga/effects';

import * as Api from '../../API';
import {
  CREATE_APPLICATION,

  applicationCreated,
} from './actions';
import {
  getWorkspace,
} from '../WorkspaceDetails/actions';

function* requestApplication() {
  const token = yield select(s => s.auth.token);
  const workspace = yield select(s => s.workspaces.details.activeWorkspace);
  const { name } = yield select(s => s.workspaces.applications.applicationForm);
  yield call(Api.requestApplication, token, workspace.id, name);
  yield put(applicationCreated());
  yield put(getWorkspace(workspace.id));
}

function* applicationRequested() {
  yield takeLatest(CREATE_APPLICATION, requestApplication);
}

export default function* root() {
  yield all([
    fork(applicationRequested),
  ])
}