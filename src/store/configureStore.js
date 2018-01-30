import {applyMiddleware, combineReducers, createStore} from 'redux';
import createSagaMiddleware from 'redux-saga';

import rootReducer from '../reducers';
import rootSaga from '../sagas';
import {reducer as reduxFormReducer} from "redux-form";


const sagaMiddleware = createSagaMiddleware();

const store = createStore(
    combineReducers({
        form: reduxFormReducer,
        rootReducer
    }),
    applyMiddleware(sagaMiddleware)
);
sagaMiddleware.run(rootSaga);

export default store;