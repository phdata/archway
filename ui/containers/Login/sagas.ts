import { all, takeLatest, call, fork, put, select } from 'redux-saga/effects';
import { delay } from 'redux-saga';
import * as Api from '../../service/api';
import * as actions from './actions';
import { SPNEGO } from '../../constants';

function* checkLogin() {
  const requestToken = localStorage.getItem('requestToken');
  if (requestToken) {
    yield put(actions.tokenExtracted(requestToken));
    yield fork(tokenReady, { token: requestToken });
  } else {
    yield put(actions.tokenNotAvailalbe());
    yield call(isSpnego);
  }
}

function* tokenReady({ token }: { token: string }) {
  const profile = yield call(Api.profile, token);
  yield put(actions.profileReady(profile));

  try {
    const workspace = yield call(Api.getPersonalWorkspace, token);
    yield put(actions.workspaceAvailable(workspace));
  } catch (exc) {
    /* tslint:disable:non-empty */
  } finally {
    yield put(actions.profileLoading(false));
  }
}

function* isSpnego() {
  try {
    const { authType } = yield call(Api.getAuthtype);
    if (authType === SPNEGO) {
      yield call(requestLogin, { username: '', password: '' });
    } else {
      // tslint:disable-next-line: no-console
      console.log('AuthType is not "spnego"');
    }
  } catch {
    // tslint:disable-next-line: no-console
    console.error('No Api Endpoint for AuthType');
  }
}

function* requestLogin({ login }: any) {
  const { username, password } = login;
  try {
    const response = yield call(Api.login, username, password);
    const { access_token, refresh_token } = response;
    localStorage.setItem('requestToken', access_token);
    localStorage.setItem('refreshToken', refresh_token);
    yield fork(tokenReady, { token: access_token });
    yield put(actions.tokenExtracted(access_token));
    yield put(actions.loginSuccess(access_token));
  } catch (error) {
    if (error.message.includes('NetworkError')) {
      yield put(actions.loginError('Error connecting to server.'));
    } else {
      yield put(actions.loginError('Invalid credentials, please try again.'));
    }
  }
}

function* loginRequested() {
  yield takeLatest(actions.LOGIN_REQUEST, requestLogin);
}

function* requestLogout() {
  yield call(Api.logout);
}

function* logoutRequested() {
  yield takeLatest(actions.LOGOUT_REQUEST, requestLogout);
}

function* profileUpdate() {
  while (true) {
    const token = yield select((s: any) => s.get('login').get('token'));
    if (token) {
      const profile = yield call(Api.profile, token);
      yield put(actions.profileReady(profile));
    }
    yield call(delay, 30000);
  }
}

function* requestWorkspace() {
  yield put(actions.profileLoading(true));
  const token = yield select((s: any) => s.get('login').get('token'));
  const workspace = yield call(Api.createWorkspace, token);
  yield put(actions.workspaceAvailable(workspace));
  yield put(actions.profileLoading(false));
}

function* workspaceRequested() {
  yield takeLatest('WORKSPACE_REQUESTED', requestWorkspace);
}

export default function* root() {
  yield all([
    fork(checkLogin),
    fork(loginRequested),
    fork(logoutRequested),
    fork(profileUpdate),
    fork(workspaceRequested),
  ]);
}
