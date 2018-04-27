import {applyMiddleware, combineReducers, compose, createStore} from 'redux';
import createSagaMiddleware from 'redux-saga';
import SagaManager from "./SagaManager";
import {routerMiddleware} from "react-router-redux";
import {reducer as reduxFormReducer} from "redux-form";
import auth from "./Auth/reducers";
import cluster from "./Navigation/reducers";
import sharedWorkspaces from "./SharedWorkspaces/reducers";
import userWorkspace from "./UserWorkspace/reducers";
import sharedWorkspaceDetails from "./SharedWorkspaceDetails/reducers";
import requestWorkspace from "./RequestProject/reducers";
import areas from "./InformationAreas/reducers";

const reducers = combineReducers({
    form: reduxFormReducer,
    auth,
    cluster,
    sharedWorkspaces,
    userWorkspace,
    workspaceDetails: sharedWorkspaceDetails,
    requestWorkspace,
    informationAreas: areas

});

export default function configureStore(history) {

    const sagaMiddleware = createSagaMiddleware();

    const routerMid = routerMiddleware(history);

    const middlewares = [sagaMiddleware, routerMid];

    const storeEnhancers = [];

    // storeEnhancers.push(window.devToolsExtension());

    const middlewareEnhancer = applyMiddleware(...middlewares);
    storeEnhancers.unshift(middlewareEnhancer);

    const store = createStore(
        reducers,
        {},
        compose(...storeEnhancers)
    );

    // run sagas
    SagaManager.startSagas(sagaMiddleware);

    if(process.env.NODE_ENV === 'development') {
        // Hot reload reducers (requires Webpack or Browserify HMR to be enabled)
        if(module.hot) {
            module.hot.accept("./Auth/reducers", () =>
                store.replaceReducer(require("./Auth/reducers").default)
            );
            module.hot.accept("./Navigation/reducers", () =>
                store.replaceReducer(require("./Navigation/reducers").default)
            );
            module.hot.accept("./SharedWorkspaces/reducers", () =>
                store.replaceReducer(require("./SharedWorkspaces/reducers").default)
            );
            module.hot.accept("./UserWorkspace/reducers", () =>
                store.replaceReducer(require("./UserWorkspace/reducers").default)
            );

            module.hot.accept('./SagaManager', () => {
                SagaManager.cancelSagas(store);
                require('./SagaManager').default.startSagas(sagaMiddleware);
            });
        }
    }

    return store;
};