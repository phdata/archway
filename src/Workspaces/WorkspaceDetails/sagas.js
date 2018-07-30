import { call, select, all, fork, takeLatest, put } from 'redux-saga/effects';

import * as Api from '../../API';
import {
  GET_WORKSPACE,

  setWorkspace
} from './actions';

function* fetchWorkspace({ id }) {
  const token = yield select(s => s.auth.token);
  const workspace = yield call(Api.getWorkspace, token, id);
  yield put(setWorkspace(workspace));
}

function* workspaceRequest() {
  yield takeLatest(GET_WORKSPACE, fetchWorkspace);
}

export default function* root() {
  yield all([
    fork(workspaceRequest),
  ])
}
