import { all, call, fork, put, select, takeLatest } from 'redux-saga/effects';
import * as Api from '../../api';
import {
  REQUEST_APPROVAL,
  GET_WORKSPACE,
  setWorkspace,
  setMembers,
  setNamespaceInfo,
  setResourcePools,
  ApprovalRequestAction,
  approvalSuccess,
} from './actions';

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
  const approval = yield call(Api.approveWorkspace, token, id, approvalType);
  yield put(approvalSuccess(approvalType, approval));
}

function* approvalRequestedListener() {
  yield takeLatest(REQUEST_APPROVAL, approvalRequested);
}

export default function* root() {
  yield all([
    fork(workspaceRequest),
    fork(approvalRequestedListener),
  ]);
}
