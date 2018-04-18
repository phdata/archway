import {all, call, fork, put, select, take} from 'redux-saga/effects'
import * as actions from "./actions";
import * as Api from "../API/index";
import {LOCATION_CHANGE} from "react-router-redux";

const getToken = state => state.auth.token;

function* loadAreas() {
    try {
        const token = yield select(getToken);
        const areas = yield call(Api.informationAreas, token);
        yield put(actions.areasSuccess(areas));
    } catch (exception) {
        yield put(actions.areasFailed(exception.message));
    }
}

function* requestAreas() {
    while (true) {
        const {payload: {pathname}} = yield take(LOCATION_CHANGE);
        if (pathname === "/workspaces") {
            yield put(actions.areasRequested());
            yield fork(loadAreas);
        }
    }
}

export default function* root() {
    yield all([
        fork(requestAreas)
    ]);
}