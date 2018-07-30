import { call, select, all, fork, takeLatest, put } from 'redux-saga/effects';
import Fuse from 'fuse.js';

import * as Api from '../../API';
import {
  GET_MEMBERS,
  MEMBER_FILTER_CHANGED,
  ROLE_CHANGED,

  setMembers,
  setFilteredList,
  memberFilterChanged,
  existingMemberSelected,
} from './actions';

const fuseOptions = {
  keys: [
    'username',
  ]
}

function* getAllMembers() {
  const token = yield select(s => s.auth.token);
  const id = yield select(s => s.workspaces.details.activeWorkspace.id);
  const members = yield call(Api.getMembers, token, id);
  yield put(setMembers(new Fuse(members, fuseOptions)));
  yield put(memberFilterChanged({ filter: { value: '' } }));
  yield put(existingMemberSelected(members[0]));
}

function* memberListRequested() {
  yield takeLatest(GET_MEMBERS, getAllMembers);
}

function* updateFilter({ filter }) {
  const existingMembers = yield select(s => s.workspaces.members.existingMembers);
  let filtered = existingMembers.list;
  if (filter && filter !== '')
    filtered = existingMembers.search(filter);
  yield put(setFilteredList(filtered));
}

function* filterChange() {
  yield takeLatest(MEMBER_FILTER_CHANGED, updateFilter);
}

function* changeRole({ username, resource, resourceId, oldRole, newRole }) {
  if (oldRole != newRole) {
    const token = yield select(s => s.auth.token);
    const id = yield select(s => s.workspaces.details.activeWorkspace.id);

    if (newRole != 'none') {
      const newMember = yield call(Api.newWorkspaceMember, token, id, resource, resourceId, newRole, username);
      yield put(existingMemberSelected(newMember));
    }

    if (oldRole)
      yield call(Api.removeWorkspaceMember, token, id, resource, resourceId, oldRole, username);
  }
}

function* roleChangeRequested() {
  yield takeLatest(ROLE_CHANGED, changeRole);
}

export default function* root() {
  yield all([
    fork(memberListRequested),
    fork(filterChange),
    fork(roleChangeRequested),
  ])
}