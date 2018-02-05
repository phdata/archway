import {all, call, fork, put, take} from 'redux-saga/effects'
import * as actions from "../actions";
import * as Api from "../api";
import {reset, stopSubmit} from "redux-form";
import {delay} from "redux-saga";

function* start() {
    const requestToken = localStorage.getItem("requestToken");
    const refreshToken = localStorage.getItem("refreshToken");
    if (requestToken)
        yield put(actions.tokenExtracted({ requestToken, refreshToken }));
    else
        yield put(actions.tokenNotAvailable());
}

function* waitForToken() {
    const token = yield take(actions.TOKEN_EXTRACTED);
    console.log(token);
}

function* authorize(username, password) {
    try {
        const {accessToken, refreshToken} =
            yield call(Api.login, username, password);
        localStorage.setItem("requestToken", accessToken);
        localStorage.setItem("refreshToken", refreshToken);
        yield put(actions.loginSuccess(accessToken));
        yield put(reset("login"));
        yield put(stopSubmit("login"));
    } catch (error) {
        yield put(actions.loginError(error));
        yield put(stopSubmit("login"));
    }
}

function* loginFlow() {
    while (true) {
        const {username, password} = yield take(actions.LOGIN_REQUEST);
        yield fork(authorize, username, password);
        yield take(actions.LOGOUT_REQUEST);
        yield call(Api.logout);
    }
}

function* getWorkspace() {
    const {requestToken} = yield take(actions.TOKEN_EXTRACTED);
    const workspace = yield call(Api.workspace, requestToken);
    if (workspace)
        yield put(actions.workspaceFound(workspace));
}

function* createWorkspace() {
    yield take(actions.WORKSPACE_REQUESTED);
    yield call(Api.requestWorkspace);
}

function* clusterStatus() {
    while (true) {
        const cluster = yield call(Api.cluster);
        yield put(actions.clusterInfo(cluster));
        yield call(delay, 30000);
    }
}

export default function* root() {
    yield all([
        fork(start),
        fork(loginFlow),
        fork(waitForToken),
        fork(getWorkspace),
        fork(createWorkspace),
        fork(clusterStatus)
    ]);
}