import { call, select, all, fork, takeLatest, put } from 'redux-saga/effects';

import * as Api from '../../API';
import {
  CREATE_TOPIC,

  topicCreated,
} from './actions';
import {
  getWorkspace,
} from '../WorkspaceDetails/actions';

function* requestTopic() {
  const token = yield select(s => s.auth.token);
  const workspace = yield select(s => s.workspaces.details.activeWorkspace);
  const { database, suffix, partitions, replicationFactor } = yield select(s => s.workspaces.topics.topicForm);
  yield call(Api.requestTopic, token, workspace.id, `${database}.${suffix}`, partitions, replicationFactor);
  yield put(topicCreated());
  yield put(getWorkspace(workspace.id));
}

function* topicRequested() {
  yield takeLatest(CREATE_TOPIC, requestTopic);
}

export default function* root() {
  yield all([
    fork(topicRequested),
  ])
}