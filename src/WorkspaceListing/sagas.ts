import { call, select, all, fork, takeLatest, put } from 'redux-saga/effects';

import * as Api from '../API';
import {workspaceListUpdated} from './actions';

function* workspaceListRequested() {
  const token = yield select((s: any) => s.get('auth').get('token'));
  const workspaces = yield call(Api.listWorkspaces, token);
  yield put(workspaceListUpdated(workspaces));
}

function* workspaceListListener() {
  yield takeLatest('LIST_ALL_WORKSPACES', workspaceListRequested);
}

export default function* root() {
  yield all([
    fork(workspaceListListener),
  ])
}