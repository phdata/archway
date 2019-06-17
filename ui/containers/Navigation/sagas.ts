import { all, call, fork, put } from 'redux-saga/effects';
import { delay } from 'redux-saga';
import * as Api from '../../service/api';
import * as actions from './actions';

function* clusterStatus() {
  while (true) {
    const cluster = yield call(Api.cluster);
    yield put(actions.clusterInfo(cluster));
    yield call(delay, 300000);
  }
}

export default function* root() {
  yield all([fork(clusterStatus)]);
}
