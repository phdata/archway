import { all, call, fork, put, select, takeLatest } from 'redux-saga/effects';
import * as Api from '../../service/api';
import { LIST_OPS_WORKSPACES, opsWorkspacesUpdated, listOpsWorkspacesFailure } from './actions';

function* opsWorkspaceListRequested() {
  const token = yield select((s: any) => s.get('login').get('token'));
  try {
    const workspaces = yield call(Api.listOpsWorkspaces, token);
    yield put(opsWorkspacesUpdated(workspaces));
  } catch (e) {
    yield put(listOpsWorkspacesFailure(e.toString()));
  }
}

function* opsWorkspaceListListener() {
  yield takeLatest(LIST_OPS_WORKSPACES, opsWorkspaceListRequested);
}

export default function* root() {
  yield all([
    fork(opsWorkspaceListListener),
  ]);
}
