import { call, select, fork, takeLatest, put } from 'redux-saga/effects';

import * as Api from '../../API';
import {
  APPROVE_WORKSPACE_REQUESTED,

  approveWorkspaceCompleted
} from './actions';

import { getWorkspace } from '../WorkspaceDetails/actions';

function* requestApproval({ role }) {
  const token = yield select(s => s.auth.token);
  const { id } = yield select(s => s.workspaces.details.activeWorkspace);
  let error;
  try {
    yield call(Api.approveWorkspace, token, id, role);
  } catch (e) {
    error = e;
  }
  yield put(approveWorkspaceCompleted(error));
  yield put(getWorkspace(id));
}

function* approveWorkspace() {
  yield takeLatest(APPROVE_WORKSPACE_REQUESTED, requestApproval);
}

export default function* root() {
  yield fork(approveWorkspace);
}