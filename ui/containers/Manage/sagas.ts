import { all, call, fork, put, select, takeLatest } from 'redux-saga/effects';
import * as Api from '../../service/api';

import {
  REQUEST_NEW_COMPLIANCE,
  setLoadingStatus,
  GET_COMPLIANCES,
  setCompliances,
  REQUEST_UPDATE_COMPLIANCE,
  REQUEST_DELETE_COMPLIANCE,
  GET_LINKSGROUPS,
  setLinksGroups,
  REQUEST_NEW_LINKSGROUP,
  REQUEST_UPDATE_LINKSGROUP,
  REQUEST_DELETE_LINKSGROUP,
} from './actions';
import { TOKEN_EXTRACTOR } from '../../constants';
import { ManagePage } from './constants';

/* tslint:disable:no-var-requires */
const router = require('connected-react-router/immutable');

export const currentComplianceExtractor = (s: any) => s.getIn(['manage', 'selectedCompliance']);
export const currentLinksGroupExtractor = (s: any) => s.getIn(['manage', 'selectedLinksGroup']);

function* newComplianceRequest() {
  const token = yield select(TOKEN_EXTRACTOR);
  const compliance = (yield select(currentComplianceExtractor)).toJS();
  try {
    yield put(setLoadingStatus(true));
    yield call(Api.requestCompliance, token, compliance);
  } catch {
    // tslint:disable-next-line: no-empty
  } finally {
    yield put(router.push({ pathname: `/manage/${ManagePage.ComplianceTab}` }));
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
    yield put(router.push({ pathname: `/manage/${ManagePage.ComplianceTab}` }));
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
    yield put(router.push({ pathname: `/manage/${ManagePage.ComplianceTab}` }));
    yield call(getCompliancesRequest);
  }
}

function* deleteComplianceRequestListener() {
  yield takeLatest(REQUEST_DELETE_COMPLIANCE, deleteComplianceRequest);
}

function* getLinksGroupsRequest() {
  const token = yield select(TOKEN_EXTRACTOR);
  try {
    yield put(setLoadingStatus(true));
    const linksGroups = yield call(Api.getLinksGroups, token);
    yield put(setLinksGroups(linksGroups));
  } catch {
    //
  } finally {
    yield put(setLoadingStatus(false));
  }
}

function* getLinksGroupsRequestListener() {
  yield takeLatest(GET_LINKSGROUPS, getLinksGroupsRequest);
}

function* newLinksGroupRequest() {
  const token = yield select(TOKEN_EXTRACTOR);
  const linksGroup = (yield select(currentLinksGroupExtractor)).toJS();
  try {
    yield put(setLoadingStatus(true));
    yield call(Api.createLinksGroup, token, linksGroup);
  } catch {
    // tslint:disable-next-line: no-empty
  } finally {
    yield put(router.push({ pathname: `/manage/${ManagePage.LinksTab}` }));
    yield call(getLinksGroupsRequest);
  }
}

function* newLinksGroupRequestListener() {
  yield takeLatest(REQUEST_NEW_LINKSGROUP, newLinksGroupRequest);
}

function* updateLinksGroupRequest() {
  const token = yield select(TOKEN_EXTRACTOR);
  const linksGroup = (yield select(currentLinksGroupExtractor)).toJS();
  try {
    yield put(setLoadingStatus(true));
    yield call(Api.updateLinksGroup, token, linksGroup, linksGroup.id);
  } catch {
    // tslint:disable-next-line: no-empty
  } finally {
    yield put(router.push({ pathname: `/manage/${ManagePage.LinksTab}` }));
    yield call(getLinksGroupsRequest);
  }
}

function* updateLinksGroupRequestListener() {
  yield takeLatest(REQUEST_UPDATE_LINKSGROUP, updateLinksGroupRequest);
}

function* deleteLinksGroupRequest() {
  const token = yield select(TOKEN_EXTRACTOR);
  const linksGroup = (yield select(currentLinksGroupExtractor)).toJS();
  try {
    yield put(setLoadingStatus(true));
    yield call(Api.deleteLinksGroup, token, linksGroup.id);
  } catch {
    // tslint:disable-next-line: no-empty
  } finally {
    yield put(router.push({ pathname: `/manage/${ManagePage.LinksTab}` }));
    yield call(getLinksGroupsRequest);
  }
}

function* deleteLinksGroupRequestListener() {
  yield takeLatest(REQUEST_DELETE_LINKSGROUP, deleteLinksGroupRequest);
}

export default function* root() {
  yield all([
    fork(newComplianceRequestListener),
    fork(getCompliancesListener),
    fork(updateComplianceRequestListener),
    fork(deleteComplianceRequestListener),
    fork(getLinksGroupsRequestListener),
    fork(newLinksGroupRequestListener),
    fork(updateLinksGroupRequestListener),
    fork(deleteLinksGroupRequestListener),
  ]);
}
