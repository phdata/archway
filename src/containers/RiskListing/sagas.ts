import { all, call, fork, put, select, takeLatest } from 'redux-saga/effects';
import * as Api from '../../service/api';
import { LIST_RISK_WORKSPACES, riskWorkspacesUpdated, listRiskWorkspacesFailure } from './actions';

function* riskWorkspaceListRequested() {
  const token = yield select((s: any) => s.get('login').get('token'));
  try {
    const workspaces = yield call(Api.listRiskWorkspaces, token);
    yield put(riskWorkspacesUpdated(workspaces));
  } catch (e) {
    yield put(listRiskWorkspacesFailure(e.toString()));
  }
}

function* riskWorkspaceListListener() {
  yield takeLatest(LIST_RISK_WORKSPACES, riskWorkspaceListRequested);
}

export default function* root() {
  yield all([
    fork(riskWorkspaceListListener),
  ]);
}
