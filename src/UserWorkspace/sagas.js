import {all, call, fork, put, select, take} from 'redux-saga/effects'
import {delay} from "redux-saga";
import * as Api from "../API";
import * as actions from "./actions";

const getToken = state => state.auth.token;

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

export default function* root() {
    yield all([
        fork(createWorkspace)
    ]);
}