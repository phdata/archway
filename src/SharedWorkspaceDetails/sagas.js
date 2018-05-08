import {all, call, fork, put, select, takeLatest} from "redux-saga/effects";
import {sharedWorkspaceDetails, WORKSPACE_MEMBER_REMOVE, workspaceMemberList} from "./actions";
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

function* fetchMembers(token, id, dataset) {
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

function* newMemberRequested() {
    yield takeLatest(WORKSPACE_MEMBER_REQUESTED, requestMember);
}

function* requestMemberRemoved({username}) {
    const token = yield select(s => s.auth.token);
    const id = yield select(s => s.workspaceDetails.workspace.id);
    yield call(Api.removeWorkspaceMember, username, token, id);
    yield fork(fetchMembers, token, id);
}

function* removeMemberRequested() {
    yield takeLatest(WORKSPACE_MEMBER_REMOVE, requestMemberRemoved)
}

export default function* root() {
    yield all([
        fork(loadDetails),
        fork(newMemberRequested),
        fork(removeMemberRequested)
    ]);
}
