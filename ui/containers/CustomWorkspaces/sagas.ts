import { all, call, fork, put, select, takeLatest } from 'redux-saga/effects';
import * as Api from '../../service/api';

import { setCustomDescriptions, LIST_CUSTOM_DESCRIPTIONS } from './actions';
import { CustomDescription } from '../../models/Template';

function* customDescriptionsRequested() {
  const token = yield select((s: any) => s.get('login').get('token'));
  try {
    const customDescriptions = yield call(Api.getCustomDescriptions, token);
    yield put(setCustomDescriptions(customDescriptions as CustomDescription[]));
  } catch {
    yield put(setCustomDescriptions([]));
  }
}

function* customDescriptionsListener() {
  yield takeLatest(LIST_CUSTOM_DESCRIPTIONS, customDescriptionsRequested);
}

export default function* root() {
  yield all([fork(customDescriptionsListener)]);
}
