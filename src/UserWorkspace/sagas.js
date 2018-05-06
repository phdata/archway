import {all, call, fork, put, select, take} from 'redux-saga/effects'
import * as Api from "../API";
import * as actions from "./actions";
import * as authActions from "../Auth/actions";
import {takeLatest} from "redux-saga/effects";
import {delay} from 'redux-saga';

const getToken = state => state.auth.token;

function* createWorkspace() {
    const token = yield select(getToken);
    yield call(Api.requestWorkspace, token);
    yield fork(getWorkspace);
}

function* createWorkspaceRequest() {
    yield takeLatest(actions.WORKSPACE_REQUESTED, createWorkspace);
}

function* getWorkspace() {
    let token = yield select(getToken);
    if(!token) {
        token = yield take(authActions.TOKEN_EXTRACTED).token;
    }
    let workspace;
    try {
        workspace = yield call(Api.workspace, token);
        yield put(actions.workspaceFound(workspace));
    } catch (exc) {
        console.log("no personal workspace found");
    }
    console.log(workspace);
    if (workspace) {
        while (!workspace.ldap || !workspace.database) {
            yield call(delay, 2000);
            workspace = yield call(Api.workspace, token);
        }
        yield put(actions.workspaceFound(workspace));
    }
}

export default function* root() {
    yield all([
        fork(createWorkspaceRequest),
        fork(getWorkspace)
    ]);
}