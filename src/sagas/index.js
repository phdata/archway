import {all, call, fork, put, select, take} from 'redux-saga/effects'
import * as actions from "../actions";
import * as Api from "../api";
import {reset, stopSubmit} from "redux-form";
import {delay} from "redux-saga";

const getToken = state => state.account.token;

function* waitForToken() {
    const token = yield take(actions.TOKEN_EXTRACTED);
    const profile = yield call(Api.profile, token);
    yield put(actions.profileReady(profile));
    yield fork(getWorkspace, token);
}

function* authorize(username, password) {
    try {
        const response =
            yield call(Api.login, username, password);
        console.log(response);
        const {access_token, refresh_token} = response;
        localStorage.setItem("requestToken", access_token);
        localStorage.setItem("refreshToken", refresh_token);
        yield put(actions.loginSuccess(access_token));
        yield put(reset("login"));
        yield put(stopSubmit("login"));
    } catch (error) {
        yield put(actions.loginError(error));
        yield put(stopSubmit("login"));
    }
}

function* loginFlow() {
    while (true) {
        const requestToken = localStorage.getItem("requestToken");
        if (requestToken)
            yield put(actions.tokenExtracted(requestToken));
        else {
            yield put(actions.tokenNotAvailalbe());
            const {username, password} = yield take(actions.LOGIN_REQUEST);
            yield fork(authorize, username, password);
        }
        yield take(actions.LOGOUT_REQUEST);
        yield call(Api.logout);
    }
}

function* getWorkspace(token) {
    try {
        const workspace = yield call(Api.workspace, token);
        yield put(actions.workspaceFound(workspace));
    } catch (exception) {
        yield put(actions.workspaceAbsent());
    }
}

function* createWorkspace() {
    yield take(actions.WORKSPACE_REQUESTED);
    const token = yield select(getToken);
    yield call(Api.requestWorkspace, token);
    let i = 0;
    let ready = false;
    while (!ready) {
        try {
            const prefer = ((i < 5) ? 404 : 200);
            const workspace = yield call(Api.workspace, token, prefer);
            yield put(actions.workspaceFound(workspace));
            ready = true;
        } catch (exception) {
            i++;
            yield call(delay, 1000);
        }
    }
}

function* clusterStatus() {
    while (true) {
        const cluster = yield call(Api.cluster);
        console.log(cluster);
        yield put(actions.clusterInfo(cluster));
        yield call(delay, 300000);
    }
}

export default function* root() {
    yield all([
        fork(loginFlow),
        fork(waitForToken),
        fork(createWorkspace),
        fork(clusterStatus)
    ]);
}