import { all, call, fork, put, select } from 'redux-saga/effects';
import { delay } from 'redux-saga';
import * as Api from '../../service/api';
import * as actions from './actions';
import { TOKEN_EXTRACTOR } from '../../constants';

export function* clusterStatus() {
  while (true) {
    const token = yield select(TOKEN_EXTRACTOR);
    yield put(actions.clusterLoading(true));
    try {
      const cluster = yield call(Api.cluster, token);
      yield put(actions.clusterInfo(cluster));
    } finally {
      yield put(actions.clusterLoading(false));
    }
    yield call(delay, 300000);
  }
}

export default function* root() {
  yield all([fork(clusterStatus)]);
}
