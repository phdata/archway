import { call, select, all, fork, takeLatest, put } from 'redux-saga/effects';

import * as Api from '../../API';
import {
  GET_MEMBERS,

  setMembers,
} from './actions';

function* getAllMembers() {
  const token = yield select(s => s.auth.token);
  const id = yield select(s => s.workspaces.details.activeWorkspace.id);
  const members = yield call(Api.getMembers, token, id);
  yield put(setMembers(members));
}

function* memberListRequested() {
  yield takeLatest(GET_MEMBERS, getAllMembers);
}

export default function* root() {
  yield all([
    fork(memberListRequested),
  ])
}
