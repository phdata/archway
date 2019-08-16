import { cancel, fork, take, call, put } from 'redux-saga/effects';
import cluster from '../containers/Navigation/sagas';
import login from '../containers/Login/sagas';
import workspace from '../containers/WorkspaceDetails/sagas';
import listing from '../containers/WorkspaceListing/sagas';
import risk from '../containers/RiskListing/sagas';
import operations from '../containers/OpsListing/sagas';
import manage from '../containers/Manage/sagas';
import request from '../containers/WorkspaceRequest/sagas';
import home from '../containers/Home/sagas';
import templates from '../containers/CustomWorkspaces/sagas';
import * as Api from '../service/api';
import * as actions from './actions';

const { config = {} } = window as any;
const isDevMode = config.isDevMode === 'true';

const sagas = [login, cluster, request, workspace, listing, risk, operations, manage, home, templates];

export const CANCEL_SAGAS_HMR = 'CANCEL_SAGAS_HMR';

const createAbortableSaga = (saga: any) => {
  if (process.env.NODE_ENV === 'development') {
    return function* main() {
      const sagaTask = yield fork(saga);

      yield take(CANCEL_SAGAS_HMR);
      yield cancel(sagaTask);
    };
  }
  return saga;
};

export function* configSaga(token: string) {
  try {
    const featureFlags = yield call(Api.getFeatureFlags, token);
    if (featureFlags) {
      yield put(actions.setFeatureFlag(featureFlags));
    } else {
      // tslint:disable-next-line: no-unused-expression
      isDevMode && console.error('Nothing returned from feature-flags API endpoint');
    }
  } catch {
    // tslint:disable-next-line: no-unused-expression
    isDevMode && console.error('No API endpoint for feature-flags');
  }
}

const SagaManager = {
  startSagas: (sagaMiddleware: any) => {
    sagas.map(createAbortableSaga).forEach(saga => sagaMiddleware.run(saga));
  },

  cancelSagas: (store: any) => {
    store.dispatch({
      type: CANCEL_SAGAS_HMR,
    });
  },
};

export default SagaManager;
