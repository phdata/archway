import { all, call, fork, put, select, takeLatest } from 'redux-saga/effects';
import * as Api from '../../api';
import { GET_WORKSPACE, setWorkspace, setMembers, setTables, setApplications } from './actions';

function* fetchWorkspace({ id }: { type: string, id: number }) {
  const token = yield select((s: any) => s.get('login').get('token'));
  const workspace = yield call(Api.getWorkspace, token, id);
  yield put(setWorkspace(workspace));

  const {members, applications, tables} = yield all({
    members: call(Api.getMembers, token, id),
    applications: call(Api.getYarnApplications, token, id),
    tables: call(Api.getHiveTables, token, id),
  });

  yield all([
    put(setMembers(members)),
    put(setTables(tables)),
    put(setApplications(applications)),
  ]);
}

function* workspaceRequest() {
  yield takeLatest(GET_WORKSPACE, fetchWorkspace);
}

export default function* root() {
  yield all([
    fork(workspaceRequest),
  ]);
}
