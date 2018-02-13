import {all, call, fork, put, select, take} from 'redux-saga/effects'
import * as actions from "./actions";
import * as Api from "../API/index";
import {LOCATION_CHANGE} from "react-router-redux";

const getToken = state => state.auth.token;

function* loadWorkspaces() {
    try {
        const token = yield select(getToken);
        const workspaces = yield call(Api.sharedWorkspaces, token);
        yield put(actions.workspacesSuccess(workspaces));
    } catch (exception) {
        yield put(actions.workspacesFailed(exception.message));
    }
}

function* requestWorkspaces() {
    while (true) {
        const {payload: {pathname}} = yield take(LOCATION_CHANGE);
        if (pathname === "/workspaces") {
            yield put(actions.workspacesRequested());
            yield fork(loadWorkspaces);
        }
    }
}

export default function* root() {
    yield all([
        fork(requestWorkspaces)
    ]);
}