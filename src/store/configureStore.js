import {applyMiddleware, compose, createStore} from 'redux';
import createSagaMiddleware from 'redux-saga';
import rootReducer from "../reducers";
import SagaManager from "../sagas/SagaManager";
import {routerMiddleware} from "react-router-redux";

export default function configureStore(history) {

    const sagaMiddleware = createSagaMiddleware();

    const routerMid = routerMiddleware(history);

    const middlewares = [sagaMiddleware, routerMid];

    const storeEnhancers = [];

    storeEnhancers.push(window.devToolsExtension());

    const middlewareEnhancer = applyMiddleware(...middlewares);
    storeEnhancers.unshift(middlewareEnhancer);

    const store = createStore(
        rootReducer,
        {},
        compose(...storeEnhancers)
    );

    // run sagas
    SagaManager.startSagas(sagaMiddleware);

    if(process.env.NODE_ENV === 'development') {
        // Hot reload reducers (requires Webpack or Browserify HMR to be enabled)
        if(module.hot) {
            module.hot.accept("../reducers/index", () =>
                store.replaceReducer(require("../reducers/index").default)
            );

            module.hot.accept('../sagas/SagaManager', () => {
                SagaManager.cancelSagas(store);
                require('../sagas/SagaManager').default.startSagas(sagaMiddleware);
            });
        }
    }

    return store;
};