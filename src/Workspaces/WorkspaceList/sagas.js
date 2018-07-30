import { call, select, all, fork, takeLatest, put } from 'redux-saga/effects';
import Fuse from 'fuse.js';

import * as Api from '../../API';
import {
  LIST_WORKSPACES,
  FILTER_CHANGED,

  setWorkspaceList,
  filterChanged,
  setFilteredList
} from './actions';

const fuseOptions = {
  keys: [
    'name',
  ]
}

function* getAllWorkspaces() {
  const token = yield select(s => s.auth.token);
  const workspaces = yield call(Api.listWorkspaces, token);
  yield put(setWorkspaceList(new Fuse(workspaces, fuseOptions)));
  yield put(filterChanged({ filter: { value: '' } }));
}

function* listWorkspaces() {
  yield takeLatest(LIST_WORKSPACES, getAllWorkspaces);
}

function* updateFilter({ filter }) {
  const workspaceList = yield select(s => s.workspaces.listing.workspaceList);
  let filtered = workspaceList.list;
  if (filter && filter !== '')
    filtered = workspaceList.search(filter);
  yield put(setFilteredList(filtered));
}

function* filterChange() {
  yield takeLatest(FILTER_CHANGED, updateFilter);
}

export default function* root() {
  yield all([
    fork(listWorkspaces),
    fork(filterChange),
  ])
}
