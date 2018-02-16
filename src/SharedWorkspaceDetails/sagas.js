import {call, put, select, takeLatest, all, fork} from "redux-saga/effects";
import {sharedWorkspaceDetails} from "./actions";
import {LOCATION_CHANGE} from "react-router-redux";
import * as Api from "../API";

function* fetchDetails({payload: {pathname}}) {
    let matches;
    if (matches = /\/workspace\/(\d+)/g.exec(pathname)) {
        const id = parseInt(matches[1]);
        const token = yield select(s => s.auth.token);
        const sharedWorkspaces = yield call(Api.sharedWorkspaces, token);
        if(sharedWorkspaces) {
            const theOne = sharedWorkspaces.find(sw => sw.id === id);
            yield put(sharedWorkspaceDetails(theOne));
        }
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