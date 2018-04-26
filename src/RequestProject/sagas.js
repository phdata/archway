import {all, call, fork, put, select, takeLatest} from "redux-saga/effects";
import {push} from "react-router-redux";
import {REQUEST_SHARED_WORKSPACE} from "./actions";
import * as Api from "../API";

function* requestWorkspace({request}) {
    console.log(request)
    const token = yield select(s => s.auth.token);
    const {id} = yield call(Api.requestNewSharedWorkspace, token, request);
    yield put(push(`/workspace/${id}`));
}

function* sharedWorkspaceRequest() {
    yield takeLatest(REQUEST_SHARED_WORKSPACE, requestWorkspace);
}

export default function* root() {
    yield all([
        fork(sharedWorkspaceRequest)
    ]);
}