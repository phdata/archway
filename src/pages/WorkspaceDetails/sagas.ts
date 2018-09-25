import { all, call, fork, put, select, takeLatest } from 'redux-saga/effects';
import * as Api from '../../api';
import { GET_WORKSPACE, setWorkspace, setMembers } from './actions';

function* fetchWorkspace({ id }: { type: string, id: number }) {
  const token = yield select((s: any) => s.get('login').get('token'));
  const workspace = yield call(Api.getWorkspace, token, id);
  yield put(setWorkspace(workspace));
  const members = yield call(Api.getMembers, token, id);
  yield put(setMembers(members));
}

function* workspaceRequest() {
  yield takeLatest(GET_WORKSPACE, fetchWorkspace);
}

export default function* root() {
  yield all([
    fork(workspaceRequest),
  ]);
}
