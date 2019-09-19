import { all, call, fork, put, select, takeLatest } from 'redux-saga/effects';
import * as Api from '../../service/api';

import {
  REQUEST_NEW_COMPLIANCE,
  setLoadingStatus,
  GET_COMPLIANCES,
  setCompliances,
  REQUEST_UPDATE_COMPLIANCE,
  REQUEST_DELETE_COMPLIANCE,
} from './actions';
import { TOKEN_EXTRACTOR } from '../../constants';
import { mockCompliances } from './constants';

/* tslint:disable:no-var-requires */
const router = require('connected-react-router/immutable');

export const currentComplianceExtractor = (s: any) => s.getIn(['manage', 'selectedCompliance']);

function* newComplianceRequest() {
  const token = yield select(TOKEN_EXTRACTOR);
  const compliance = (yield select(currentComplianceExtractor)).toJS();
  try {
    yield put(setLoadingStatus(true));
    yield call(Api.requestCompliance, token, compliance);
  } catch {
    // tslint:disable-next-line: no-empty
  } finally {
    yield put(router.push({ pathname: '/manage' }));
    yield call(getCompliancesRequest);
  }
}

function* newComplianceRequestListener() {
  yield takeLatest(REQUEST_NEW_COMPLIANCE, newComplianceRequest);
}

function* getCompliancesRequest() {
  const token = yield select(TOKEN_EXTRACTOR);
  try {
    yield put(setLoadingStatus(true));
    const compliances = yield call(Api.getCompliances, token);
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

function* updateComplianceRequest() {
  const token = yield select(TOKEN_EXTRACTOR);
  const compliance = (yield select(currentComplianceExtractor)).toJS();
  try {
    yield put(setLoadingStatus(true));
    yield call(Api.updateCompliance, token, compliance.id, compliance);
  } catch {
    // tslint:disable-next-line: no-empty
  } finally {
    yield put(router.push({ pathname: '/manage' }));
    yield call(getCompliancesRequest);
  }
}

function* updateComplianceRequestListener() {
  yield takeLatest(REQUEST_UPDATE_COMPLIANCE, updateComplianceRequest);
}

function* deleteComplianceRequest() {
  const token = yield select(TOKEN_EXTRACTOR);
  const compliance = (yield select(currentComplianceExtractor)).toJS();
  try {
    yield put(setLoadingStatus(true));
    yield call(Api.deleteCompliance, token, compliance.id);
  } catch {
    // tslint:disable-next-line: no-empty
  } finally {
    yield put(router.push({ pathname: '/manage' }));
    yield call(getCompliancesRequest);
  }
}

function* deleteComplianceRequestListener() {
  yield takeLatest(REQUEST_DELETE_COMPLIANCE, deleteComplianceRequest);
}

export default function* root() {
  yield all([
    fork(newComplianceRequestListener),
    fork(getCompliancesListener),
    fork(updateComplianceRequestListener),
    fork(deleteComplianceRequestListener),
  ]);
}
