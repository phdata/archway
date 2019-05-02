import { all, call, fork, put, select, takeLatest } from 'redux-saga/effects';
import * as Api from '../../service/api';
import {
  LIST_ALL_WORKSPACES,
  REFRESH_RECENT_WORKSPACES,
  workspaceListUpdated,
  setRecentWorkspaces,
} from './actions';

function* workspaceListRequested() {
  const token = yield select((s: any) => s.get('login').get('token'));
  const workspaces = yield call(Api.listWorkspaces, token);
  yield put(workspaceListUpdated(workspaces));
}

function* workspaceListListener() {
  yield takeLatest(LIST_ALL_WORKSPACES, workspaceListRequested);
}

function* refreshRecentWorkspaces() {
  const token = yield select((s: any) => s.get('login').get('token'));
  const recentWorkspaces = yield select((s: any) => s.get('listing').get('recent'));
  const workspaces = yield all(
    recentWorkspaces.toJS().map(({ id }: { id: number }) => call(Api.getWorkspace, token, id))
  );
  yield put(setRecentWorkspaces(workspaces));
}

function* refreshRecentWorkspacesListener() {
  yield takeLatest(REFRESH_RECENT_WORKSPACES, refreshRecentWorkspaces);
}

export default function* root() {
  yield all([
    fork(workspaceListListener),
    fork(refreshRecentWorkspacesListener),
  ]);
}
