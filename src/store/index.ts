const router = require('connected-react-router/immutable');
import { History } from 'history';
import { applyMiddleware, compose, createStore } from 'redux';
import createSagaMiddleware from 'redux-saga';
import reducers from './reducers';
import SagaManager from './sagas';

const store = (history: History) => {
  const composeEnhancers = (<any>window).__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;
  const sagaMiddleware = createSagaMiddleware();

  const enhancer = composeEnhancers(
    applyMiddleware(
      sagaMiddleware,
      router.routerMiddleware(history),
    ),
  );

  const result = createStore(
    router.connectRouter(history)(reducers),
    enhancer,
  );

  SagaManager.startSagas(sagaMiddleware);

  return result;
}

export default store;