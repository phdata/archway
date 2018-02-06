import {all, call, fork, put, take} from 'redux-saga/effects'
import * as actions from "../actions";
import * as Api from "../api";
import {reset, stopSubmit} from "redux-form";
import {delay} from "redux-saga";

function* waitForToken() {
    const token = yield take(actions.TOKEN_EXTRACTED);
    console.log(token);
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
            const {username, password} = yield take(actions.LOGIN_REQUEST);
            yield fork(authorize, username, password);
        }
        yield take(actions.LOGOUT_REQUEST);
        yield call(Api.logout);
    }
}

function* getWorkspace() {
    const {token} = yield take(actions.TOKEN_EXTRACTED);
    try {
        const workspace = yield call(Api.workspace, token);
        if (workspace)
            yield put(actions.workspaceFound(workspace));
    } catch(exception) {
        yield put(actions.workspaceAbsent());
    }
}

function* createWorkspace() {
    yield take(actions.WORKSPACE_REQUESTED);
    yield call(Api.requestWorkspace);
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
        fork(getWorkspace),
        fork(createWorkspace),
        fork(clusterStatus)
    ]);
}