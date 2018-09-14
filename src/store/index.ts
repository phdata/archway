const router = require('connected-react-router/immutable');
import { History } from 'history';
import { fromJS } from 'immutable';
import { applyMiddleware, compose, createStore, Store } from 'redux';
import { combineReducers } from 'redux-immutable';
import createSagaMiddleware from 'redux-saga';
import auth from '../Auth/reducers';
import cluster from '../Navigation/reducers';
import workspaceList from '../WorkspaceListing/reducers';
import request from '../WorkspaceRequest/reducers';
import { StoreState } from '../types';
import workspaces from '../Workspaces/reducers';
import SagaManager from './sagas';

const store: (history: History) => Store<StoreState> = (history) => {
  const composeEnhancers = (<any>window).__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;
  const sagaMiddleware = createSagaMiddleware();

  const reducers = combineReducers({
    auth,
    workspaceList,
    request,
    cluster,
    workspaces,
  });

  const enhancer = composeEnhancers(
    applyMiddleware(
      sagaMiddleware,
      router.routerMiddleware(history),
    ),
  );

  const result = createStore(
    router.connectRouter(history)(reducers),
    fromJS({}),
    enhancer,
  );

  SagaManager.startSagas(sagaMiddleware);

  return result;
}

export default store;