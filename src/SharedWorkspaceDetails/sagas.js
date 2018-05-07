import {all, call, fork, put, select, takeLatest} from "redux-saga/effects";
import {sharedWorkspaceDetails, workspaceMemberList} from "./actions";
import {LOCATION_CHANGE} from "react-router-redux";
import {WORKSPACE_MEMBER_REQUESTED} from "./actions";
import * as Api from "../API";
import {delay} from "redux-saga";

function* fetchDetails({payload: {pathname}}) {
    const matches = /\/workspace\/(\d+)/g.exec(pathname);
    if (matches) {
        const id = parseInt(matches[1], 10);
        const token = yield select(s => s.auth.token);
        yield fork(workspaceDetails, token, id);
        yield fork(fetchMembers, token, id);
    }
}

function* fetchMembers(token, id) {
    const members = yield call(Api.workspaceMemberList, token, id);
    yield put(workspaceMemberList(members));
}

function* workspaceDetails(token, id) {
    let workspace = yield call(Api.sharedWorkspaceDetails, token, id);
    yield put(sharedWorkspaceDetails(workspace));

    while (!workspace.ldap || !workspace.data) {
        yield call(delay, 2000);
        workspace = yield call(Api.sharedWorkspaceDetails, token, id);
        if (workspace.ldap || workspace.data) {
            yield put(sharedWorkspaceDetails(workspace));
        }
    }
    yield put(sharedWorkspaceDetails(workspace));
}

function* loadDetails() {
    yield takeLatest(LOCATION_CHANGE, fetchDetails);
}

function* requestMember({username}) {
    const id = yield select(s => s.workspaceDetails.workspace.id);
    const token = yield select(s => s.auth.token);
    yield call(Api.workspaceNewMember, username, token, id);
    yield fork(fetchMembers, token, id);
}

function* newMemberRequeste() {
    yield takeLatest(WORKSPACE_MEMBER_REQUESTED, requestMember);
}

export default function* root() {
    yield all([
        fork(loadDetails),
        fork(newMemberRequeste)
    ]);
}
