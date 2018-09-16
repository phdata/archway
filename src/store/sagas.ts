import { cancel, fork, take } from 'redux-saga/effects';
import cluster from '../components/Navigation/sagas';
import login from '../pages/Login/sagas';
import workspace from '../pages/WorkspaceDetails/sagas';
import listing from '../pages/WorkspaceListing/sagas';
import request from '../pages/WorkspaceRequest/sagas';

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
