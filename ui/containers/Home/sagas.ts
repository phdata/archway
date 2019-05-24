import { all, call, fork, put, select, take, takeLatest } from 'redux-saga/effects';
import {
  RECENT_WORKSPACES_KEY,
  TOKEN_EXTRACTOR,
} from '../../constants'
import { Workspace, Member } from '../../models/Workspace';
import * as Api from '../../service/api';
import * as actions from './actions';
import { PROFILE_READY } from '../Login/actions';

function* refreshRecentWorkspaces() {
  const token = yield select(TOKEN_EXTRACTOR);
  let profile = yield select( (s: any) => s.getIn(['login', 'profile']));

  if (profile === false) {
    profile = (yield take(PROFILE_READY)).profile;
  }

  const saved = JSON.parse(localStorage.getItem(RECENT_WORKSPACES_KEY) || '[]');
  const members = {};
  saved.forEach( (workspace: Workspace) =>
    members[workspace.id] = call(Api.getMembers, token, workspace.id)
  )
  const permissions = yield all(members);
  const result: any[] = [];
  Object.keys(permissions).forEach( (key: string) => {
    if (permissions[key].filter((m: Member) => m.distinguished_name === profile.distinguished_name).length > 0) {
      result.push(call(Api.getWorkspace, token, Number(key)))
    }
  });
  const workspaces: Workspace[] = yield all(result);
  yield put(actions.setRecentWorkspaces(workspaces.reverse()));
}

function* refreshRecentWorkspacesListener() {
  yield takeLatest(actions.REFRESH_RECENT_WORKSPACES, refreshRecentWorkspaces);
}

export default function* root() {
  yield all([
    fork(refreshRecentWorkspaces),
    fork(refreshRecentWorkspacesListener),
  ]);
}
