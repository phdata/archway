import { cancel, fork, take } from 'redux-saga/effects';
import cluster from '../containers/Navigation/sagas';
import login from '../containers/Login/sagas';
import workspace from '../containers/WorkspaceDetails/sagas';
import listing from '../containers/WorkspaceListing/sagas';
import request from '../containers/WorkspaceRequest/sagas';

const sagas = [
  login,
  cluster,
  request,
  workspace,
  listing,
];

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

const SagaManager = ({
  startSagas: (sagaMiddleware: any) => {
    sagas.map(createAbortableSaga).forEach((saga) => sagaMiddleware.run(saga));
  },

  cancelSagas: (store: any) => {
    store.dispatch({
      type: CANCEL_SAGAS_HMR,
    });
  },
});

export default SagaManager;
