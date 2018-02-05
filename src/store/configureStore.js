import {applyMiddleware, combineReducers, createStore} from 'redux';
import createSagaMiddleware from 'redux-saga';

import account from '../reducers/account';
import cluster from "../reducers/cluster";
import rootSaga from '../sagas';
import {reducer as reduxFormReducer} from "redux-form";

const sagaMiddleware = createSagaMiddleware();

const store = createStore(
    combineReducers({
        form: reduxFormReducer,
        account,
        cluster
    }),
    applyMiddleware(sagaMiddleware)
);
sagaMiddleware.run(rootSaga);

export default store;