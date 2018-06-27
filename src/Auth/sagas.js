import { reset, stopSubmit } from 'redux-form';
import { all, call, cancel, fork, put, take, select, takeLatest } from 'redux-saga/effects';
import * as Api from '../API';
import * as actions from './actions';
import { LOGIN_FAILURE, LOGOUT_REQUEST } from './actions';
import { push } from 'react-router-redux';

function* authorize(username, password) {
  try {
    const response = yield call(Api.login, username, password);
    const { access_token, refresh_token } = response;
    localStorage.setItem('requestToken', access_token);
    localStorage.setItem('refreshToken', refresh_token);
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
    if (requestToken) { yield put(actions.tokenExtracted(requestToken)); } else {
      yield put(actions.tokenNotAvailalbe());
      yield take(actions.LOGIN_REQUEST);
      const { username, password } = yield select(s => s.auth.loginForm);
      task = yield fork(authorize, username, password);
    }
    const action = yield take([LOGOUT_REQUEST, LOGIN_FAILURE]);
    if (action.type === LOGOUT_REQUEST && task) { yield cancel(task); }
    yield call(Api.logout);
    yield put(push('/personal'));
  }
}

function* tokenReady({ token }) {
  const profile = yield call(Api.profile, token);
  yield put(actions.profileReady(profile));
}

function* waitForToken() {
  yield takeLatest(actions.TOKEN_EXTRACTED, tokenReady);
}

export default function* root() {
  yield all([
    fork(loginFlow),
    fork(waitForToken),
  ]);
}
