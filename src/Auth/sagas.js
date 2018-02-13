import {reset, stopSubmit} from "redux-form";
import {fork, take, call, put, all} from "redux-saga/effects";
import * as Api from "../API";
import * as actions from "./actions";
import * as workspaceActions from "../UserWorkspace/actions";

function* authorize(username, password) {
    try {
        const response = yield call(Api.login, username, password);
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

function* waitForToken() {
    const token = yield take(actions.TOKEN_EXTRACTED);
    const profile = yield call(Api.profile, token);
    yield put(actions.profileReady(profile));
    yield fork(getWorkspace, token);
}

function* getWorkspace(token) {
    try {
        const workspace = yield call(Api.workspace, token);
        yield put(workspaceActions.workspaceFound(workspace));
    } catch (exception) {
        yield put(workspaceActions.workspaceAbsent());
    }
}

export default function* root() {
    yield all([
        fork(loginFlow),
        fork(waitForToken)
    ]);
}