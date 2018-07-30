import { call, select, all, fork, takeLatest, put } from 'redux-saga/effects';

import * as Api from '../../API';
import {
  CREATE_TOPIC,

  getAllTopics
} from './actions';

function* requestTopic({ database, suffix, partitions, replicationFactor }) {
  const token = yield select(s => s.auth.token);
  const workspace = yield select(s => s.workspaces.details.activeWorkspace);
  yield call(Api.requestTopic, token, workspace.id, `${database.suffix}`, partitions, replicationFactor);
  yield put(getAllTopics());
}

function* topicRequested() {
  yield takeLatest(CREATE_TOPIC, requestWorkspace);
}

export default function* root() {
  yield all([
    fork(typeChanged),
    fork(requestChanged),
    fork(inputChanged),
    fork(workspaceRequested)
  ])
}