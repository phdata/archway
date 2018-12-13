import { all, call, fork, put, select, takeLatest } from 'redux-saga/effects';
import * as Api from '../../service/api';
import { LIST_ALL_WORKSPACES, workspaceListUpdated } from './actions';

function* workspaceListRequested() {
  const token = yield select((s: any) => s.get('login').get('token'));
  const workspaces = yield call(Api.listWorkspaces, token);
  yield put(workspaceListUpdated(workspaces));
}

function* workspaceListListener() {
  yield takeLatest(LIST_ALL_WORKSPACES, workspaceListRequested);
}

export default function* root() {
  yield all([
    fork(workspaceListListener),
  ]);
}
