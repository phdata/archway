import {all, call, fork, put, select, takeLatest} from "redux-saga/effects";
import {governedDatasetDetails} from "./actions";
import {LOCATION_CHANGE} from "react-router-redux";
import * as Api from "../API";

function* fetchDetails({payload: {pathname}}) {
    const matches = /\/dataset\/(\d+)/g.exec(pathname);
    if (matches) {
        const id = parseInt(matches[1], 10);
        const token = yield select(s => s.auth.token);
        const details = yield call(Api.datasetDetails, token, id);
        yield put(governedDatasetDetails(details));
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
