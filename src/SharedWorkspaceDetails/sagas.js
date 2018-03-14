import {call, put, select, takeLatest, all, fork} from "redux-saga/effects";
import {sharedWorkspaceDetails, workspaceMemberList} from "./actions";
import {LOCATION_CHANGE} from "react-router-redux";
import * as Api from "../API";

function* fetchDetails({payload: {pathname}}) {
    const matches = /\/workspace\/(\d+)/g.exec(pathname);
    if (matches) {
        const id = parseInt(matches[1], 10);
        const token = yield select(s => s.auth.token);
        const details = yield call(Api.sharedWorkspaceDetails, token, id);
        yield put(sharedWorkspaceDetails(details));
        const members = yield call(Api.workspaceMemberList, token, id);
        yield put(workspaceMemberList(members));
    }
}

function* loadDetails() {
    yield takeLatest(LOCATION_CHANGE, fetchDetails);
}

export default function* root() {
    yield all([
        fork(loadDetails)
    ]);
}
