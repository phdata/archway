import {reset, stopSubmit} from "redux-form";
import {fork, take, call, put, all, cancel} from "redux-saga/effects";
import * as Api from "../API";
import * as actions from "./actions";
import {LOGIN_FAILURE} from "./actions";

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
    let task;
    while (true) {
        const requestToken = localStorage.getItem("requestToken");
        if (requestToken)
            yield put(actions.tokenExtracted(requestToken));
        else {
            yield put(actions.tokenNotAvailalbe());
            const {username, password} = yield take(actions.LOGIN_REQUEST);
            task = yield fork(authorize, username, password);
        }
        const action = yield take(['LOGOUT', 'LOGIN_FAILURE'])
        if (action.type === 'LOGOUT' && task)
            yield cancel(task)
        yield call(Api.logout);
    }
}

function* waitForToken() {
    const {token} = yield take(actions.TOKEN_EXTRACTED);
    const profile = yield call(Api.profile, token);
    yield put(actions.profileReady(profile));
}

export default function* root() {
    yield all([
        fork(loginFlow),
        fork(waitForToken)
    ]);
}