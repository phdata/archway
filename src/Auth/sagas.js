import { reset, stopSubmit } from 'redux-form';
import { all, takeLatest, call, cancel, fork, put, take, select } from 'redux-saga/effects';
import { delay } from 'redux-saga';
import * as Api from '../API';
import * as actions from './actions';

function* authorize(username, password) {
  try {
    const response = yield call(Api.login, username, password);
    const { access_token, refresh_token } = response;
    localStorage.setItem('requestToken', access_token);
    localStorage.setItem('refreshToken', refresh_token);
    yield fork(tokenReady, { token: access_token });
    yield put(actions.tokenExtracted(access_token));
    yield put(actions.loginSuccess(access_token));
    yield put(reset('login'));
    yield put(stopSubmit('login'));
  } catch (error) {
    yield put(actions.loginError('Invalid credentials, please try again.'));
    yield put(stopSubmit('login'));
  }
}

function* loginFlow() {
  let task;
  while (true) {
    const requestToken = localStorage.getItem('requestToken');
    if (requestToken) {
      yield put(actions.tokenExtracted(requestToken));
      yield fork(tokenReady, { token: requestToken });
    } else {
      yield put(actions.tokenNotAvailalbe());
      yield take(actions.LOGIN_REQUEST);
      const { username, password } = yield select(s => s.auth.loginForm);
      task = yield fork(authorize, username, password);
    }
    const action = yield take([actions.LOGOUT_REQUEST, actions.LOGIN_FAILURE]);
    if (action.type === actions.LOGOUT_REQUEST && task) { yield cancel(task); }
    yield call(Api.logout);
  }
}

function* tokenReady({ token }) {
  const profile = yield call(Api.profile, token);
  yield put(actions.profileReady(profile));

  try {
    const workspace = yield call(Api.getPersonalWorkspace, token);
    yield put(actions.workspaceAvailable(workspace));
  } catch(exc) {

  } finally {
    yield put(actions.profileLoading(false));
  }
}

function* profileUpdate() {
  while (true) {
    const token = yield select(s => s.get('auth').get('token'));
    if (token) {
      const profile = yield call(Api.profile, token);
      yield put(actions.profileReady(profile));
    }
    yield call(delay, 30000);
  }
}

function* requestWorkspace() {
  yield put(actions.profileLoading(true));
  const token = yield select(s => s.get('auth').get('token'));
  const workspace = yield call(Api.createWorkspace, token);
  yield put(actions.workspaceAvailable(workspace));
  yield put(actions.profileLoading(false));
}

function* workspaceRequested() {
  yield takeLatest('WORKSPACE_REQUESTED', requestWorkspace);
}

export default function* root() {
  yield all([
    fork(loginFlow),
    fork(profileUpdate),
    fork(workspaceRequested),
  ]);
}
