import { applyMiddleware, compose, createStore, Store, Reducer } from 'redux';
import { combineReducers } from 'redux-immutable';
import createSagaMiddleware from 'redux-saga';

import SagaManager from './sagas';
import auth from '../Auth/reducers';
import cluster from '../Navigation/reducers';
import workspaces from '../Workspaces/reducers';
import workspaceList from '../WorkspaceListing/reducers';
import { StoreState } from '../types';
import { fromJS } from 'immutable';
import {createHashHistory} from 'history';
import {routerMiddleware} from 'connected-react-router';

const composeEnhancers = (<any>window).__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;
const sagaMiddleware = createSagaMiddleware();

const reducers = combineReducers({
  auth,
  workspaceList,
  cluster,
  workspaces,
});

const enhancer = composeEnhancers(
  applyMiddleware(sagaMiddleware),
);

const store: Store<StoreState> = createStore<StoreState>(
  reducers as Reducer<StoreState>,
  fromJS({}),
  enhancer,
);

SagaManager.startSagas(sagaMiddleware);

export default store;