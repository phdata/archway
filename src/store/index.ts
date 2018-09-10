import { applyMiddleware, combineReducers, compose, createStore } from 'redux';
import createSagaMiddleware from 'redux-saga';

import SagaManager from '../SagaManager';
import auth from '../Auth/reducers';
import cluster from '../Navigation/reducers';
import workspaces from '../Workspaces/reducers';

const composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;
const sagaMiddleware = createSagaMiddleware();

const reducers = combineReducers({
  auth,
  cluster,
  workspaces,
});

const enhancer = compose(
  applyMiddleware(sagaMiddleware),
);

const store = createStore(
  reducers,
  {auth: {loading: false}},
  enhancer,
);

SagaManager.startSagas(sagaMiddleware);

export default store;