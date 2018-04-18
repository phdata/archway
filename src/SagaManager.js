import { take, fork, cancel } from 'redux-saga/effects';

import auth from "./Auth/sagas";
import cluster from "./Navigation/sagas";
import userWorkspaace from "./UserWorkspace/sagas";
import sharedWorkspaceDetails from "./SharedWorkspaceDetails/sagas";
import requestWorkspace from "./RequestProject/sagas";
import sharedWorkspaces from "./SharedWorkspaces/sagas";
import informationAreas from "./InformationAreas/sagas";

const sagas = [
    auth,
    cluster,
    sharedWorkspaces,
    userWorkspaace,
    sharedWorkspaceDetails,
    requestWorkspace,
    informationAreas
];

export const CANCEL_SAGAS_HMR = 'CANCEL_SAGAS_HMR';

function createAbortableSaga (saga) {
    if (process.env.NODE_ENV === 'development') {
        return function* main () {
            const sagaTask = yield fork(saga);

            yield take(CANCEL_SAGAS_HMR);
            yield cancel(sagaTask);
        };
    } else {
        return saga;
    }
}

const SagaManager = {
    startSagas(sagaMiddleware) {
        sagas.map(createAbortableSaga).forEach((saga) => sagaMiddleware.run(saga));
    },

    cancelSagas(store) {
        store.dispatch({
            type: CANCEL_SAGAS_HMR
        });
    }
};

export default SagaManager;