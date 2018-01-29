import {createStore, applyMiddleware, combineReducers} from 'redux';
import createSagaMiddleware from 'redux-saga';

import rootReducer from '../reducers';
import rootSagas from '../sagas';
import { routerReducer } from 'react-router-redux';

const sagaMiddleware = createSagaMiddleware();

const combined = (root) => {
    return combineReducers({
        root,
        routing: routerReducer
    });
};

const configureStore = preloadedState => {

    if (module.hot) {
        // Enable Webpack hot module replacement for reducers
        module.hot.accept('../reducers', () => {
            const nextRootReducer = require('../reducers').default;
            store.replaceReducer(nextRootReducer);
        });
    }

    return store;
};

export default configureStore;