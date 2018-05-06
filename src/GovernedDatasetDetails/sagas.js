import {all, call, fork, put, select, takeLatest} from "redux-saga/effects";
import {governedDatasetDetails} from "./actions";
import {LOCATION_CHANGE} from "react-router-redux";
import * as Api from "../API";
import {delay} from "redux-saga";
import {take} from "redux-saga/effects";
import * as authActions from "../Auth/actions";

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

    let workspace = yield call(Api.datasetDetails, token, id);
    yield put(governedDatasetDetails(workspace));

    while (!workspace.raw.ldap || !workspace.raw.data ||
            !workspace.staging.ldap || !workspace.staging.data ||
            !workspace.modeled.ldap || !workspace.modeled.data) {
        yield call(delay, 2000);
        workspace = yield call(Api.datasetDetails, token, id);
        if (workspace.raw.ldap || workspace.raw.data ||
            workspace.staging.ldap || workspace.staging.data ||
            workspace.modeled.ldap || workspace.modeled.data) {
            yield put(governedDatasetDetails(workspace));
        }
    }
    yield put(governedDatasetDetails(workspace));
}

function* loadDetails() {
    yield takeLatest(LOCATION_CHANGE, fetchDetails);
}

export default function* root() {
    yield all([
        fork(loadDetails)
    ]);
}
