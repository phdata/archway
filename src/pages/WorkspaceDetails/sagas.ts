import { all, call, fork, put, select, takeLatest, takeEvery } from 'redux-saga/effects';
import * as Api from '../../api';
import {
  REQUEST_APPROVAL,
  REQUEST_REMOVE_MEMBER,
  GET_WORKSPACE,
  setWorkspace,
  setMembers,
  setNamespaceInfo,
  setResourcePools,
  ApprovalRequestAction,
  approvalSuccess,
  REQUEST_TOPIC,
  topicRequestSuccess,
  SIMPLE_MEMBER_REQUEST,
  simpleMemberRequestComplete,
  getWorkspace,
  approvalFailure,
  RemoveMemberRequestAction,
  removeMemberSuccess,
  removeMemberFailure,
} from './actions';
import { Workspace } from '../../types/Workspace';

function* fetchWorkspace({ id }: { type: string, id: number }) {
  const token = yield select((s: any) => s.get('login').get('token'));
  const workspace = yield call(Api.getWorkspace, token, id);
  yield put(setWorkspace(workspace));

  const { members, resourcePools, infos } = yield all({
    members: call(Api.getMembers, token, id),
    resourcePools: call(Api.getYarnApplications, token, id),
    infos: call(Api.getHiveTables, token, id),
  });

  yield all([
    put(setMembers(members)),
    put(setNamespaceInfo(infos)),
    put(setResourcePools(resourcePools)),
  ]);
}

function* workspaceRequest() {
  yield takeLatest(GET_WORKSPACE, fetchWorkspace);
}

function* approvalRequested({ approvalType }: ApprovalRequestAction) {
  const token = yield select((s: any) => s.get('login').get('token'));
  const { id } = yield select((s: any) => s.get('details').get('details').toJS());
  try {
    const approval = yield call(Api.approveWorkspace, token, id, approvalType);
    yield put(approvalSuccess(approvalType, approval));
  } catch (e) {
    yield put(approvalFailure(approvalType, e.toString()));
  }
}

function* approvalRequestedListener() {
  yield takeLatest(REQUEST_APPROVAL, approvalRequested);
}

function* topicRequested() {
  const token = yield select((s: any) => s.get('login').get('token'));
  const { id } = yield select((s: any) => s.get('details').get('details').toJS());
  const { name } = yield select((s: any) => s.getIn(['form', 'topicRequest', 'values']).toJS());
  yield call(Api.requestTopic, token, id, name, 1, 1);
  yield put(topicRequestSuccess());
  yield put(getWorkspace(id));
}

function* topicRequestedListener() {
  yield takeLatest(REQUEST_TOPIC, topicRequested);
}

function* simpleMemberRequested() {
  const token = yield select((s: any) => s.get('login').get('token'));
  const workspace: Workspace = yield select<Workspace>((s: any) => s.get('details').get('details').toJS() as Workspace);
  const { username } = yield select((s: any) => s.getIn(['form', 'simpleMemberRequest', 'values']).toJS());
  const { applications, data, topics } = {
    applications: workspace.applications.map(({ id }) => ({ type: 'applications', id })),
    data: workspace.data.map(({ id }) => ({ type: 'data', id })),
    topics: workspace.topics.map(({ id }) => ({ type: 'topics', id })),
  };
  const allResources = applications.concat(data).concat(topics);
  yield all(
    allResources.map(({ type, id }) => (
      call(Api.newWorkspaceMember, token, workspace.id, type, id, 'manager', username)
    )),
  );
  yield put(simpleMemberRequestComplete());
  const members = yield call(Api.getMembers, token, workspace.id);
  yield put(setMembers(members));
}

function* simpleMemberRequestedListener() {
  yield takeLatest(SIMPLE_MEMBER_REQUEST, simpleMemberRequested);
}

function* removeMemberRequested({ username }: RemoveMemberRequestAction) {
  const token = yield select((s: any) => s.get('login').get('token'));
  const workspace: Workspace = yield select<Workspace>((s: any) => s.get('details').get('details').toJS() as Workspace);
  const { applications, data, topics } = {
    applications: workspace.applications.map(({ id }) => ({ type: 'applications', id })),
    data: workspace.data.map(({ id }) => ({ type: 'data', id })),
    topics: workspace.topics.map(({ id }) => ({ type: 'topics', id })),
  };
  const allResources = applications.concat(data).concat(topics);
  try {
    yield all(
      allResources.map(({ type, id }) => (
        call(Api.removeWorkspaceMember, token, workspace.id, type, id, 'manager', username)
      )),
    );
    yield put(removeMemberSuccess(username));
  } catch (e) {
    yield put(removeMemberFailure(username, e.toString()));
  }
}

function* removeMemberRequestedListener() {
  yield takeEvery(REQUEST_REMOVE_MEMBER, removeMemberRequested);
}

export default function* root() {
  yield all([
    fork(workspaceRequest),
    fork(approvalRequestedListener),
    fork(topicRequestedListener),
    fork(simpleMemberRequestedListener),
    fork(removeMemberRequestedListener),
  ]);
}
