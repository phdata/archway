import {all, call, fork, put, select, takeLatest} from "redux-saga/effects";
import {LOCATION_CHANGE} from "react-router-redux";
import * as Api from "../API";
import {delay} from "redux-saga";
import {take} from "redux-saga/effects";
import * as authActions from "../Auth/actions";
import {
    DATASET_MEMBER_REMOVE,
    DATASET_MEMBER_REQUESTED,
    governedDatasetDetails,
    datasetMemberList, GOVERNED_DATASET_SELECTED
} from "./actions";

const getToken = state => state.auth.token;

function* fetchDetails({payload: {pathname}}) {
    const matches = /\/dataset\/(\d+)/g.exec(pathname);
    if (matches) {
        const id = parseInt(matches[1], 10);
        yield fork(datasetDetails, id);
    }
}

function* datasetDetails(id) {
    let token = yield select(getToken);
    if(!token) {
        token = yield take(authActions.TOKEN_EXTRACTED);
    }

    let dataset = yield call(Api.datasetDetails, token, id);
    yield put(governedDatasetDetails(dataset));
    yield fork(fetchMembers, token, id, "raw");

    while (!dataset.raw.ldap || !dataset.raw.data ||
            !dataset.staging.ldap || !dataset.staging.data ||
            !dataset.modeled.ldap || !dataset.modeled.data) {
        yield call(delay, 2000);
        dataset = yield call(Api.datasetDetails, token, id);
        if (dataset.raw.ldap || dataset.raw.data ||
            dataset.staging.ldap || dataset.staging.data ||
            dataset.modeled.ldap || dataset.modeled.data) {
            yield put(governedDatasetDetails(dataset));
        }
    }
    yield put(governedDatasetDetails(dataset));
}

function* fetchMembers(token, id, dataset) {
    const members = yield call(Api.datasetMemberList, token, id, dataset);
    yield put(datasetMemberList(members));
}

function* loadDetails() {
    yield takeLatest(LOCATION_CHANGE, fetchDetails);
}

function* requestMember({username}) {
    const id = yield select(s => s.datasetDetails.dataset.id);
    const token = yield select(s => s.auth.token);
    const dataset = yield select(s => s.datasetDetails.active.name);
    yield call(Api.datasetNewMember, username, token, id, dataset);
    yield fork(fetchMembers, token, id, dataset);
}

function* newMemberRequested() {
    yield takeLatest(DATASET_MEMBER_REQUESTED, requestMember);
}

function* requestMemberRemoved({username}) {
    const token = yield select(s => s.auth.token);
    const id = yield select(s => s.datasetDetails.dataset.id);
    const dataset = yield select(s => s.datasetDetails.active.name);
    yield call(Api.removeDatasetMember, username, token, id, dataset);
    yield fork(fetchMembers, token, id, dataset);
}

function* removeMemberRequested() {
    yield takeLatest(DATASET_MEMBER_REMOVE, requestMemberRemoved)
}

function* updateMemberList({name: dataset}) {
    const token = yield select(s => s.auth.token);
    const id = yield select(s => s.datasetDetails.dataset.id);
    yield fork(fetchMembers, token, id, dataset);
}

function* tabChanged() {
    yield takeLatest(GOVERNED_DATASET_SELECTED, updateMemberList)
}

export default function* root() {
    yield all([
        fork(loadDetails),
        fork(newMemberRequested),
        fork(removeMemberRequested),
        fork(tabChanged)
    ]);
}
