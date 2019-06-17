import { all, call, fork, put, select, takeLatest } from 'redux-saga/effects';
import * as Api from '../../service/api';
import {
  setLoading,
  setTemplate,
  setWorkspace,
  setCurrentPage,
  clearRequest,
  SET_BEHAVIOR,
  GOTO_NEXT_PAGE,
  GOTO_PREV_PAGE,
  createWorkspaceRequest,
  createWorkspaceFailure,
} from './actions';
import { PAGE_BEHAVIOR, PAGE_DETAILS, PAGE_COMPLIANCE, PAGE_REVIEW } from './constants';

/* tslint:disable:no-var-requires */
const router = require('connected-react-router/immutable');

function* behaviorChangedListener({ behavior }: any) {
  yield put(setLoading(true));
  const token = yield select((s: any) => s.get('login').get('token'));
  const templateDefaults = yield call(Api.getTemplate, token, behavior);
  yield put(setTemplate(templateDefaults));
  yield put(setLoading(false));
}

function* behaviorChanged() {
  yield takeLatest(SET_BEHAVIOR, behaviorChangedListener);
}

function* nextPageListener() {
  const currentPage = yield select((s: any) => s.get('request').get('currentPage'));
  if (currentPage === PAGE_BEHAVIOR) {
    yield put(setCurrentPage(PAGE_DETAILS));
  } else if (currentPage === PAGE_DETAILS) {
    yield put(setCurrentPage(PAGE_COMPLIANCE));
  } else if (currentPage === PAGE_COMPLIANCE) {
    const request = yield select((s: any) =>
      s
        .get('request')
        .get('request')
        .toJS()
    );
    if (request) {
      yield put(setLoading(true));
      const token = yield select((s: any) => s.get('login').get('token'));
      const requestType = yield select((s: any) => s.get('request').get('behavior'));
      const template = yield select((s: any) => s.get('request').get('template'));
      const workspaceRequest = Object.assign({}, template, request);
      const workspace = yield call(Api.processTemplate, token, requestType, workspaceRequest);
      yield put(setWorkspace(workspace));
      yield put(setLoading(false));
      yield put(setCurrentPage(PAGE_REVIEW));
    }
  } else if (currentPage === PAGE_REVIEW) {
    const token = yield select((s: any) => s.get('login').get('token'));
    const workspace = yield select((s: any) => s.get('request').get('workspace'));
    yield put(createWorkspaceRequest());
    try {
      const newWorkspace = yield call(Api.requestWorkspace, token, workspace);
      yield put(router.push({ pathname: `/workspaces/${newWorkspace.id}` }));
      yield put(clearRequest());
    } catch (e) {
      yield put(createWorkspaceFailure(e.toString()));
    }
  }
}

function* nextPage() {
  yield takeLatest(GOTO_NEXT_PAGE, nextPageListener);
}

function* prevPageListener() {
  const currentPage = yield select((s: any) => s.get('request').get('currentPage'));
  if (currentPage === PAGE_REVIEW) {
    yield put(setCurrentPage(PAGE_COMPLIANCE));
  } else if (currentPage === PAGE_COMPLIANCE) {
    yield put(setCurrentPage(PAGE_DETAILS));
  } else if (currentPage === PAGE_DETAILS) {
    yield put(setCurrentPage(PAGE_BEHAVIOR));
  }
}

function* prevPage() {
  yield takeLatest(GOTO_PREV_PAGE, prevPageListener);
}

export default function* root() {
  yield all([fork(behaviorChanged), fork(nextPage), fork(prevPage)]);
}
