import {all, call, fork, put, select, takeLatest} from "redux-saga/effects";
import {push} from "react-router-redux";
import {REQUEST_GOVERNED_DATASET} from "./actions";
import * as Api from "../API";

function* submitRequest({request}) {
    console.log(request);
    const token = yield select(s => s.auth.token);
    const {id} = yield call(Api.requestDataset, token, request);
    yield put(push(`/dataset/${id}`));
}

function* requestGovernedDataset() {
    yield takeLatest(REQUEST_GOVERNED_DATASET, submitRequest);
}

export default function* root() {
    yield all([
        fork(requestGovernedDataset)
    ]);
}