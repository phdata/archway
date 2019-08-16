import { all, call, fork, put, select, takeLatest } from 'redux-saga/effects';
import * as Api from '../../service/api';

import {
  REQUEST_NEW_COMPLIANCE,
  setLoadingStatus,
  GET_COMPLIANCES,
  setCompliances,
  CLEAR_COMPLIANCES,
  clearCompliances,
} from './actions';
import { TOKEN_EXTRACTOR } from '../../constants';
import { mockCompliances } from './constants';

/* tslint:disable:no-var-requires */
const router = require('connected-react-router/immutable');

export const newComplianceExtractor = (s: any) => s.getIn(['manage', 'selectedCompliance']);

function* newComplianceRequest() {
  const token = yield select(TOKEN_EXTRACTOR);
  const compliance = (yield select(newComplianceExtractor)).toJS();
  try {
    yield put(setLoadingStatus(true));
    yield call(Api.requestCompliance, token, compliance);
  } catch {
    // tslint:disable-next-line: no-empty
  } finally {
    yield put(router.push({ pathname: '/manage' }));
    yield put(setLoadingStatus(false));
  }
}

function* newComplianceRequestListener() {
  yield takeLatest(REQUEST_NEW_COMPLIANCE, newComplianceRequest);
}

function* getCompliancesRequest() {
  const token = yield select(TOKEN_EXTRACTOR);
  yield put(clearCompliances());
  try {
    yield put(setLoadingStatus(true));
    const { compliances } = yield call(Api.getCompliances, token);
    yield put(setCompliances(compliances));
  } catch {
    yield put(setCompliances(mockCompliances));
    //
  } finally {
    yield put(setLoadingStatus(false));
  }
}

function* getCompliancesListener() {
  yield takeLatest(GET_COMPLIANCES, getCompliancesRequest);
}

function* clearCompliancesRequest() {
  yield put(setCompliances([]));
}

function* clearCompliancesListener() {
  yield takeLatest(CLEAR_COMPLIANCES, clearCompliancesRequest);
}

export default function* root() {
  yield all([fork(newComplianceRequestListener), fork(getCompliancesListener), fork(clearCompliancesListener)]);
}
